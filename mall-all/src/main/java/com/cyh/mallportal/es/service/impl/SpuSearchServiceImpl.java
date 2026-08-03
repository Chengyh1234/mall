package com.cyh.mallportal.es.service.impl;

import com.cyh.mallportal.es.entity.SpuIndex;
import com.cyh.mallportal.es.repository.SpuIndexConverter;
import com.cyh.mallportal.es.repository.SpuIndexRepository;
import com.cyh.mallportal.es.service.SpuSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SpuSearchServiceImpl implements SpuSearchService {

    @Autowired
    private SpuIndexRepository spuIndexRepository;

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

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 关键词搜索（分词匹配 name^3 + category_name^1.5 + brand_name^1.5 + store_name^1.5 + description^1）
        if (StringUtils.hasText(keyword)) {
            boolQuery.must(QueryBuilders.multiMatchQuery(keyword.trim(),
                    "name^3", "category_name^1.5", "brand_name^1.5", "store_name^1.5", "description"));
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
        // 价格区间
        if (minPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("min_price").gte(minPrice.doubleValue()));
        }
        if (maxPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("min_price").lte(maxPrice.doubleValue()));
        }

        // 默认只返回上架商品（status=1）
        boolQuery.filter(QueryBuilders.termQuery("status", 1));

        // 2. 构建 SearchSourceBuilder
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .query(boolQuery)
                .from(from)
                .size(size);

        // 3. 排序
        String field = sortBy;
        if (!StringUtils.hasText(field) || !ALLOWED_SORT_FIELDS.contains(field)) {
            field = "created_at";
        }
        // 将 price 映射为 ES 字段 min_price
        if ("price".equals(field)) {
            field = "min_price";
        }
        SortOrder order = "asc".equalsIgnoreCase(sortOrder) ? SortOrder.ASC : SortOrder.DESC;
        sourceBuilder.sort(SortBuilders.fieldSort(field).order(order));

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

        long total = response.getHits().getTotalHits().value;

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