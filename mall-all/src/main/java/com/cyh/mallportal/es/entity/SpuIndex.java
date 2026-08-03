package com.cyh.mallportal.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ES 商品索引映射实体
 * <p>
 * 对应 ES 索引 mall_spu，用于商品搜索、筛选、排序和搜索建议。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SpuIndex {

    /** SPU 主键 */
    private Long id;

    /** 商品名称（IK 最大分词，用于关键词搜索） */
    private String name;

    /** 商品描述（IK 最大分词） */
    private String description;

    /** 关键词 */
    private String keywords;

    /** 分类 ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 品牌 ID */
    private Long brandId;

    /** 品牌名称 */
    private String brandName;

    /** 店铺 ID */
    private Long storeId;

    /** 店铺名称 */
    private String storeName;

    /** 商家 ID */
    private Long sellerId;

    /** 最低售价 */
    private BigDecimal minPrice;

    /** 销量 */
    private Integer sales;

    /** 主图路径 */
    private String mainImage;

    /** 状态（1-上架 0-下架） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /**
     * 构建 ES 索引映射（Mapping）
     * <p>
     * name / description 使用 IK 分词器（最大粒度），
     * suggest 字段用于搜索建议（Completion Suggester）。
     */
    public static XContentBuilder buildMapping() throws IOException {
        return XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("properties")
                        .startObject("id").field("type", "long").endObject()
                        .startObject("name")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_smart")
                        .endObject()
                        .startObject("description")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_smart")
                        .endObject()
                        .startObject("keywords")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_smart")
                        .endObject()
                        .startObject("category_id").field("type", "long").endObject()
                        .startObject("category_name")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_smart")
                        .endObject()
                        .startObject("brand_id").field("type", "long").endObject()
                        .startObject("brand_name")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_smart")
                        .endObject()
                        .startObject("store_id").field("type", "long").endObject()
                        .startObject("store_name")
                            .field("type", "text")
                            .field("analyzer", "ik_max_word")
                            .field("search_analyzer", "ik_smart")
                        .endObject()
                        .startObject("seller_id").field("type", "long").endObject()
                        .startObject("min_price").field("type", "double").endObject()
                        .startObject("sales").field("type", "integer").endObject()
                        .startObject("main_image").field("type", "keyword").endObject()
                        .startObject("status").field("type", "integer").endObject()
                        .startObject("created_at").field("type", "date")
                            .field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
                        .endObject()
                        .startObject("suggest").field("type", "completion").endObject()
                    .endObject()
                .endObject();
    }

    /**
     * 构建 ES 索引设置（Settings）
     */
    public static Settings buildSettings() {
        return Settings.builder()
                //设置主片
                .put("index.number_of_shards", 1)
                //设置分片
                .put("index.number_of_replicas", 1)
                .build();
    }
}