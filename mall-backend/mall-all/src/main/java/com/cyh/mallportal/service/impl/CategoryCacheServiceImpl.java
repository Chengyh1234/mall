package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.service.CategoryCacheService;
import com.cyh.mallportal.vo.CategoryTreeVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类缓存服务实现类
 * <p>
 * 只缓存根节点分类树（category:tree），访问 /category/tree 时从根节点构建整棵树并缓存。
 * 分类增删改时清除缓存，下次访问自动回源重建。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCacheServiceImpl implements CategoryCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<CategoryTreeVo> getTreeWithChildren() {
        try {
            String json = stringRedisTemplate.opsForValue().get(RedisConstants.CATEGORY_TREE_KEY);
            if (json != null) {
                log.debug("从缓存获取分类树成功");
                return objectMapper.readValue(json, new TypeReference<List<CategoryTreeVo>>() {});
            }
        } catch (JsonProcessingException e) {
            log.error("反序列化分类树缓存失败, 异常: {}", e.getMessage());
            stringRedisTemplate.delete(RedisConstants.CATEGORY_TREE_KEY);
        }
        return null;
    }

    @Override
    public void setTreeWithChildren(List<CategoryTreeVo> tree) {
        try {
            String json = objectMapper.writeValueAsString(tree);
            stringRedisTemplate.opsForValue().set(RedisConstants.CATEGORY_TREE_KEY, json);
            log.debug("设置分类树缓存成功（永久）");
        } catch (JsonProcessingException e) {
            log.error("序列化分类树缓存失败, 异常: {}", e.getMessage());
        }
    }

    @Override
    public void clearAllCategoryCache() {
        Boolean deleted = stringRedisTemplate.delete(RedisConstants.CATEGORY_TREE_KEY);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("清除分类树缓存成功");
        }
    }
}