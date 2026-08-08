package com.cyh.mallportal.es.repository;

import com.cyh.mallportal.es.entity.SpuIndex;
import org.elasticsearch.search.SearchHit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES 商品索引转换器
 * <p>
 * 负责 SpuIndex 实体与 ES 文档（Map / SearchHit）之间的双向转换。
 */
public class SpuIndexConverter {

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private SpuIndexConverter() {}

    /**
     * SpuIndex → Map（用于 IndexRequest / UpdateRequest）
     */
    public static Map<String, Object> toMap(SpuIndex index) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", index.getId());
        map.put("name", index.getName());
        map.put("description", index.getDescription());
        map.put("keywords", index.getKeywords());
        map.put("category_id", index.getCategoryId());
        map.put("category_name", index.getCategoryName());
        map.put("brand_id", index.getBrandId());
        map.put("brand_name", index.getBrandName());
        map.put("store_id", index.getStoreId());
        map.put("store_name", index.getStoreName());
        map.put("seller_id", index.getSellerId());
        map.put("min_price", index.getMinPrice() != null ? index.getMinPrice().doubleValue() : null);
        map.put("sales", index.getSales());
        map.put("main_image", index.getMainImage());
        //map.put("status", index.getStatus());
        map.put("created_at", index.getCreatedAt() != null ? index.getCreatedAt().format(DT_FORMATTER) : null);
        // suggest 字段使用 name,description,category_name,brand_name,store_name 作为补全输入
        Map<String, Object> suggest = new HashMap<>();
        List<String> inputs = new ArrayList<>();
        if(index.getName()!=null) {
            inputs.add(index.getName());
        }
        if(index.getDescription()!=null) {
            inputs.add(index.getDescription());
        }
        if(index.getBrandName()!=null) {
            inputs.add(index.getBrandName());
        }
        if(index.getCategoryName()!=null) {
            inputs.add(index.getCategoryName());
        }
        if(index.getStoreName()!=null) {
            inputs.add(index.getStoreName());
        }
        suggest.put("input", inputs);
        map.put("suggest", suggest);

        return map;
    }

    /**
     * SearchHit → SpuIndex（用于搜索结果反序列化）
     */
    public static SpuIndex fromHit(SearchHit hit) {
        Map<String, Object> source = hit.getSourceAsMap();
        if (source == null) {
            return null;
        }
        SpuIndex index = new SpuIndex();
        index.setId(toLong(source.get("id")));
        index.setName(toString(source.get("name")));
        index.setDescription(toString(source.get("description")));
        index.setKeywords(toString(source.get("keywords")));
        index.setCategoryId(toLong(source.get("category_id")));
        index.setCategoryName(toString(source.get("category_name")));
        index.setBrandId(toLong(source.get("brand_id")));
        index.setBrandName(toString(source.get("brand_name")));
        index.setStoreId(toLong(source.get("store_id")));
        index.setStoreName(toString(source.get("store_name")));
        index.setSellerId(toLong(source.get("seller_id")));
        index.setMinPrice(toBigDecimal(source.get("min_price")));
        index.setSales(toInteger(source.get("sales")));
        index.setMainImage(toString(source.get("main_image")));
        //index.setStatus(toInteger(source.get("status")));
        index.setCreatedAt(toLocalDateTime(toString(source.get("created_at"))));
        return index;
    }

    // ==================== 类型转换工具 ====================

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.valueOf(v.toString());
    }

    private static Integer toInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        return Integer.valueOf(v.toString());
    }

    private static String toString(Object v) {
        return v != null ? v.toString() : null;
    }

    private static BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        return BigDecimal.valueOf(((Number) v).doubleValue());
    }

    private static LocalDateTime toLocalDateTime(String v) {
        if (v == null) return null;
        return LocalDateTime.parse(v, DT_FORMATTER);
    }
}