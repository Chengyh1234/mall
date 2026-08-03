package com.cyh.mallportal.es.service;

import com.cyh.mallportal.es.entity.SpuIndex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品 ES 搜索服务接口
 * <p>
 * 提供关键词搜索、多维度筛选、排序、分页和搜索建议功能。
 * 替代原有的 MySQL LIKE 模糊搜索。
 */
public interface SpuSearchService {

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
    Map<String, Object> search(String keyword, Long categoryId, Long brandId, Long storeId,
                               BigDecimal minPrice, BigDecimal maxPrice,
                               String sortBy, String sortOrder,
                               Integer page, Integer pageSize) throws IOException;

    /**
     * 搜索建议（Completion Suggester）
     *
     * @param prefix 用户输入的前缀
     * @param size   返回的建议数量
     * @return 建议词列表
     */
    List<String> suggest(String prefix, int size) throws IOException;
}