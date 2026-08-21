package com.cyh.mallproduct.es.service.impl;

import com.cyh.mallproduct.es.entity.SpuIndex;
import com.cyh.mallproduct.es.repository.SpuIndexConverter;
import com.cyh.mallproduct.es.repository.SpuIndexRepository;
import com.cyh.mallproduct.es.service.SpuSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品 ES 搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuSearchServiceImpl implements SpuSearchService {

    private final SpuIndexRepository spuIndexRepository;

    /** 允许排序的字段集合 */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("sales", "price", "created_at");

    /** 默认分页大小 */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 商品搜索（关键词 + 筛选 + 排序 + 分页）
     *
     * @param keyword    搜索关键词（可选，匹配 name、description）
     * @param categoryId 分类 ID（可选，精确筛选）
     * @param brandId    品牌 ID（可选，精确筛选）
     * @param storeId    店铺 ID（可选，精确筛选）
     * @param minPrice   最低售价下限（可选）
     * @param maxPrice   最低售价上限（可选）
     * @param sortBy     排序字段：sales / price / created_at，默认 created_at
     * @param sortOrder  排序方向：asc / desc，默认 desc
     * @param page       页码，从 1 开始
     * @param pageSize   每页条数
     * @return { list: SpuIndex[], total: long, page: int, pageSize: int }
     */
    @Override
    public Map<String, Object> search(String keyword, Long categoryId, Long brandId, Long storeId,
                                      BigDecimal minPrice, BigDecimal maxPrice,
                                      String sortBy, String sortOrder,
                                      Integer page, Integer pageSize) throws IOException {
        // 1. 构建查询
        int pageNum = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : pageSize;
        int from = (pageNum - 1) * size;

        // 【深度分页保护】防止超大页码导致 ES OOM
        int maxOffset = 10000;
        if (from > maxOffset) {
            throw new IllegalArgumentException("页码超过最大限制，请使用更精准的关键词或调整筛选条件");
        }

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();


        // 关键词搜索（分词匹配 name^3 + category_name^1.5 + brand_name^1.5 + store_name^1.5 + description^1）
        if (StringUtils.hasText(keyword)) {
            // cyhcandel: 无法使用,这个无法分配权重
            //boolQuery.must(QueryBuilders.multiMatchQuery(keyword.trim(),
            //        "name^3", "category_name^1.5", "brand_name^1.5", "store_name^1.5", "description^1"));
            boolQuery.must(QueryBuilders.multiMatchQuery
                    (keyword.trim()).
                    field("name",3.0f)
                    .field("category_name",1.5f)
                    .field("brand_name",1.5f)
                    .field("store_name",1.5f)
                    .field("description",1.0f)

            );
        }
        // 筛选条件（filter 不参与评分，性能更好）
        if (categoryId != null) {
            boolQuery.filter(QueryBuilders.termQuery("category_id", categoryId));
        }
        if (brandId != null) {
            boolQuery.filter(QueryBuilders.termQuery("brand_id", brandId));
        }
        if (storeId != null) {
            boolQuery.filter(QueryBuilders.termQuery("store_id", storeId));
        }

        // 【修正】价格区间 – 判断商品价格区间与用户区间是否有交集,SpuIndex只有min_price
        if (minPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("min_price").gte(minPrice.doubleValue()));
        }
        if (maxPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("min_price").lte(maxPrice.doubleValue()));
        }

        // cyhcandel :es中不需要status了
        // 默认只返回上架商品（status=1）
        //boolQuery.filter(QueryBuilders.termQuery("status", 1));

        // 2. 构建 SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .from(from)
                .size(size);

        // 3. 排序
        // 用户是否指定了排序字段
        boolean hasUserSort = StringUtils.hasText(sortBy) && ALLOWED_SORT_FIELDS.contains(sortBy);

        if (StringUtils.hasText(keyword)) {
            // ---------- 有关键词 ----------
            if (hasUserSort) {
                // 【核心修正】用户主动选价格/新品 -> 屏蔽 _score，完全按用户指定字段排序
                // 这样用户点"价格从低到高"时，最便宜的商品一定出现在第一页第一条
                String esField = "price".equals(sortBy) ? "min_price" : sortBy;
                SortOrder order = "asc".equalsIgnoreCase(sortOrder) ? SortOrder.ASC : SortOrder.DESC;
                sourceBuilder.sort(esField, order);
            } else {
                // 默认（用户未选排序）：相关性优先 + 新品辅助（综合排序）
                sourceBuilder.sort("_score", SortOrder.DESC);
                sourceBuilder.sort("created_at", SortOrder.DESC);
            }
        } else {
            // ---------- 无关键词（纯浏览模式） ----------
            if (hasUserSort) {
                // 浏览模式下，用户主动筛选价格/新品，同样完全忽略 _score（因为没有关键词，_score 全是 1.0，无意义）
                String esField = "price".equals(sortBy) ? "min_price" : sortBy;
                SortOrder order = "asc".equalsIgnoreCase(sortOrder) ? SortOrder.ASC : SortOrder.DESC;
                sourceBuilder.sort(esField, order);
            } else {
                // 默认按创建时间降序（最新上架优先）
                sourceBuilder.sort("created_at", SortOrder.DESC);
            }
        }

        // 4. 执行搜索
        SearchResponse response = spuIndexRepository.search(sourceBuilder);

        // 5. 解析结果
        List<SpuIndex> list = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            SpuIndex spuIndex = SpuIndexConverter.fromHit(hit);
            if (spuIndex != null) {
                list.add(spuIndex);
            }
        }

        long total = Objects.requireNonNull(response.getHits().getTotalHits()).value;

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", pageNum);
        result.put("pageSize", size);

        log.debug("ES 搜索完成: keyword={}, total={}", keyword, total);
        return result;
    }

    /**
     * 搜索建议（Completion Suggester）
     *
     * @param prefix 用户输入的前缀
     * @param size   返回的建议数量
     * @return 建议词列表
     */
    @Override
    public List<String> suggest(String prefix, int size) throws IOException {
        if (!StringUtils.hasText(prefix)) {
            return Collections.emptyList();
        }
        int suggestSize = (size < 1) ? 5 : size;

        SearchResponse response = spuIndexRepository.suggest(prefix.trim(), suggestSize);

        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return Collections.emptyList();
        }

        CompletionSuggestion completionSuggestion = suggest.getSuggestion("spu_suggest");
        if (completionSuggestion == null) {
            return Collections.emptyList();
        }

        return completionSuggestion.getEntries().stream()
                .flatMap(entry -> entry.getOptions().stream())
                .map(option -> option.getText().string())
                .distinct()
                .limit(suggestSize)
                .collect(Collectors.toList());
    }
}