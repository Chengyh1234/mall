package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.entity.Category;
import com.cyh.mallportal.service.CategoryCacheService;
import com.cyh.mallportal.vo.CategoryTreeVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 分类缓存服务实现类
 * 
 * <p>该类负责管理分类数据的Redis缓存，主要用于优化分类查询性能。
 * 通过将频繁访问的分类数据缓存到Redis中，可以显著减少数据库查询次数，
 * 提升系统响应速度。</p>
 * 
 * <p>缓存策略：
 * <ul>
 *   <li>缓存过期时间：60分钟</li>
 *   <li>缓存键格式：category:{type}:{key}</li>
 *   <li>缓存清除时机：分类增删改操作时自动清除所有分类缓存</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCacheServiceImpl implements CategoryCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 分类缓存键前缀 - 单个分类
     */
    private static final String CATEGORY_ID_PREFIX = "category:id:";

    /**
     * 分类缓存键前缀 - 子分类列表
     */
    private static final String CATEGORY_PARENT_PREFIX = "category:parent:";

    /**
     * 分类缓存键前缀 - 按级别分类
     */
    private static final String CATEGORY_LEVEL_PREFIX = "category:level:";

    /**
     * 分类缓存键前缀 - 按状态分类
     */
    private static final String CATEGORY_STATUS_PREFIX = "category:status:";

    /**
     * 分类缓存键前缀 - 分类树
     */
    private static final String CATEGORY_TREE_PREFIX = "category:tree:";

    /**
     * 分类缓存键前缀 - 列表查询
     */
    private static final String CATEGORY_LIST_PREFIX = "category:list:";

    /**
     * 分类缓存键前缀 - 分页查询
     */
    private static final String CATEGORY_PAGE_PREFIX = "category:page:";

    /**
     * 分类缓存策略说明：
     * 1. 所有分类缓存默认不设置过期时间，永久存在
     * 2. 只有在分类数据发生变化（新增/更新/删除）时才清除缓存
     * 3. 这种策略适用于分类数据修改频率极低的场景，可以最大化缓存命中率
     */

    @Override
    public Category getCategoryById(Long id) {
        String cacheKey = CATEGORY_ID_PREFIX + id;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取分类成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, Category.class);
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化分类缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setCategoryById(Category category) {
        String cacheKey = CATEGORY_ID_PREFIX + category.getId();
        try {
            String json = objectMapper.writeValueAsString(category);
            // 不设置过期时间，缓存永久存在，数据变化时手动清除
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置分类缓存成功（永久），缓存键: {}", cacheKey);
        } catch (JsonProcessingException e) {
            log.error("序列化分类缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public List<Category> getChildrenByParentId(Long parentId) {
        String cacheKey = CATEGORY_PARENT_PREFIX + (parentId == null ? 0 : parentId);
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取子分类列表成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<Category>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化子分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setChildrenByParentId(Long parentId, List<Category> children) {
        String cacheKey = CATEGORY_PARENT_PREFIX + (parentId == null ? 0 : parentId);
        try {
            String json = objectMapper.writeValueAsString(children);
            // 不设置过期时间，缓存永久存在，数据变化时手动清除
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置子分类列表缓存成功（永久），缓存键: {}, 子分类数量: {}", cacheKey, children.size());
        } catch (JsonProcessingException e) {
            log.error("序列化子分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public List<Category> getByLevel(Integer level) {
        String cacheKey = CATEGORY_LEVEL_PREFIX + level;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取级别分类列表成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<Category>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化级别分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setByLevel(Integer level, List<Category> categories) {
        String cacheKey = CATEGORY_LEVEL_PREFIX + level;
        try {
            String json = objectMapper.writeValueAsString(categories);
            // 不设置过期时间，缓存永久存在，数据变化时手动清除
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置级别分类列表缓存成功（永久），缓存键: {}, 分类数量: {}", cacheKey, categories.size());
        } catch (JsonProcessingException e) {
            log.error("序列化级别分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public List<Category> getByStatus(Integer status) {
        String cacheKey = CATEGORY_STATUS_PREFIX + status;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取状态分类列表成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<Category>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化状态分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setByStatus(Integer status, List<Category> categories) {
        String cacheKey = CATEGORY_STATUS_PREFIX + status;
        try {
            String json = objectMapper.writeValueAsString(categories);
            // 不设置过期时间，缓存永久存在，数据变化时手动清除
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置状态分类列表缓存成功（永久），缓存键: {}, 分类数量: {}", cacheKey, categories.size());
        } catch (JsonProcessingException e) {
            log.error("序列化状态分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public List<CategoryTreeVo> getTreeWithChildren(Long parentId) {
        String cacheKey = CATEGORY_TREE_PREFIX + (parentId == null ? 0 : parentId);
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取分类树成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<CategoryTreeVo>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化分类树缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setTreeWithChildren(Long parentId, List<CategoryTreeVo> tree) {
        String cacheKey = CATEGORY_TREE_PREFIX + (parentId == null ? 0 : parentId);
        try {
            String json = objectMapper.writeValueAsString(tree);
            // 不设置过期时间，缓存永久存在，数据变化时手动清除
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置分类树缓存成功（永久），缓存键: {}", cacheKey);
        } catch (JsonProcessingException e) {
            log.error("序列化分类树缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public void clearAllCategoryCache() {
        // 清除单个分类缓存
        var idKeys = redisTemplate.keys(CATEGORY_ID_PREFIX + "*");
        if (idKeys != null && !idKeys.isEmpty()) {
            redisTemplate.delete(idKeys);
            log.info("清除所有单个分类缓存成功，共清除 {} 个缓存", idKeys.size());
        }

        // 清除子分类列表缓存
        var parentKeys = redisTemplate.keys(CATEGORY_PARENT_PREFIX + "*");
        if (parentKeys != null && !parentKeys.isEmpty()) {
            redisTemplate.delete(parentKeys);
            log.info("清除所有子分类列表缓存成功，共清除 {} 个缓存", parentKeys.size());
        }

        // 清除级别分类缓存
        var levelKeys = redisTemplate.keys(CATEGORY_LEVEL_PREFIX + "*");
        if (levelKeys != null && !levelKeys.isEmpty()) {
            redisTemplate.delete(levelKeys);
            log.info("清除所有级别分类缓存成功，共清除 {} 个缓存", levelKeys.size());
        }

        // 清除状态分类缓存
        var statusKeys = redisTemplate.keys(CATEGORY_STATUS_PREFIX + "*");
        if (statusKeys != null && !statusKeys.isEmpty()) {
            redisTemplate.delete(statusKeys);
            log.info("清除所有状态分类缓存成功，共清除 {} 个缓存", statusKeys.size());
        }

        // 清除分类树缓存
        var treeKeys = redisTemplate.keys(CATEGORY_TREE_PREFIX + "*");
        if (treeKeys != null && !treeKeys.isEmpty()) {
            redisTemplate.delete(treeKeys);
            log.info("清除所有分类树缓存成功，共清除 {} 个缓存", treeKeys.size());
        }

        // 清除列表查询缓存
        var listKeys = redisTemplate.keys(CATEGORY_LIST_PREFIX + "*");
        if (listKeys != null && !listKeys.isEmpty()) {
            redisTemplate.delete(listKeys);
            log.info("清除所有分类列表缓存成功，共清除 {} 个缓存", listKeys.size());
        }

        // 清除分页查询缓存
        var pageKeys = redisTemplate.keys(CATEGORY_PAGE_PREFIX + "*");
        if (pageKeys != null && !pageKeys.isEmpty()) {
            redisTemplate.delete(pageKeys);
            log.info("清除所有分类分页缓存成功，共清除 {} 个缓存", pageKeys.size());
        }
    }

    @Override
    public void clearCategoryCache(Long id) {
        // 清除单个分类缓存
        redisTemplate.delete(CATEGORY_ID_PREFIX + id);
        // 清除其父分类的子分类列表缓存（因为该分类可能是父分类的子分类）
        Category category = getCategoryById(id);
        if (category != null && category.getParentId() != null) {
            redisTemplate.delete(CATEGORY_PARENT_PREFIX + category.getParentId());
        }
        // 清除该分类的子分类列表缓存（如果有子分类）
        redisTemplate.delete(CATEGORY_PARENT_PREFIX + id);
        log.debug("清除分类相关缓存成功，分类ID: {}", id);
    }

    @Override
    public List<Category> getCategoryList(String cacheKey) {
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(json)) {
                log.debug("从缓存获取分类列表成功，缓存键: {}", cacheKey);
                return objectMapper.readValue(json, new TypeReference<List<Category>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    @Override
    public void setCategoryList(String cacheKey, List<Category> categories) {
        try {
            String json = objectMapper.writeValueAsString(categories);
            // 不设置过期时间，缓存永久存在，数据变化时手动清除
            redisTemplate.opsForValue().set(cacheKey, json);
            log.debug("设置分类列表缓存成功（永久），缓存键: {}, 分类数量: {}", cacheKey, categories.size());
        } catch (JsonProcessingException e) {
            log.error("序列化分类列表缓存失败，缓存键: {}, 异常: {}", cacheKey, e.getMessage());
        }
    }

    @Override
    public String generatePageCacheKey(Category category, Integer page, Integer pageSize) {
        StringBuilder sb = new StringBuilder(CATEGORY_PAGE_PREFIX);
        if (category != null) {
            sb.append("id:").append(category.getId() == null ? "all" : category.getId());
            sb.append(":name:").append(StringUtils.hasText(category.getName()) ? category.getName().hashCode() : "none");
            sb.append(":parentId:").append(category.getParentId() == null ? "all" : category.getParentId());
            sb.append(":level:").append(category.getLevel() == null ? "all" : category.getLevel());
            sb.append(":status:").append(category.getStatus() == null ? "all" : category.getStatus());
        } else {
            sb.append("id:all:name:none:parentId:all:level:all:status:all");
        }
        sb.append(":p:").append(page).append(":ps:").append(pageSize);
        return sb.toString();
    }

    @Override
    public String generateListCacheKey(Category category) {
        StringBuilder sb = new StringBuilder(CATEGORY_LIST_PREFIX);
        if (category != null) {
            sb.append("id:").append(category.getId() == null ? "all" : category.getId());
            sb.append(":name:").append(StringUtils.hasText(category.getName()) ? category.getName().hashCode() : "none");
            sb.append(":parentId:").append(category.getParentId() == null ? "all" : category.getParentId());
            sb.append(":level:").append(category.getLevel() == null ? "all" : category.getLevel());
            sb.append(":status:").append(category.getStatus() == null ? "all" : category.getStatus());
        } else {
            sb.append("id:all:name:none:parentId:all:level:all:status:all");
        }
        return sb.toString();
    }
}