package com.cyh.mallcommon.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 轻量级工具类（基于 StringRedisTemplate 手动序列化）
 * <p>
 * 所有 value 均为纯 JSON 字符串，手动控制序列化/反序列化：
 * - 避免 Jackson2JsonRedisSerializer 反序列化时丢成 LinkedHashMap
 * - 避免 GenericJackson2JsonRedisSerializer 的 @class 类型注入风险
 * - 类型由调用代码显式控制，类型安全且数据是纯 JSON，不绑定 Java 类名
 *
 * @author cyh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // ==================== JSON 序列化 / 反序列化 ====================

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param obj 待序列化对象
     * @return JSON 字符串，异常时返回 null
     */
    public String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化对象失败, class: {}", obj.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 反序列化后的对象，异常时返回 null
     */
    public <T> T fromJson(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("反序列化 JSON 失败, class: {}", clazz.getSimpleName(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为泛型集合/复杂类型
     * <p>
     * 适用场景：List&lt;Brand&gt;、Map&lt;String, Long&gt; 等带泛型参数的类型
     * <pre>{@code
     *     List<Brand> brands = redisUtils.fromJson(json, new TypeReference<List<Brand>>() {});
     * }</pre>
     *
     * @param json         JSON 字符串
     * @param typeReference 类型引用
     * @param <T>          泛型
     * @return 反序列化后的对象，异常时返回 null
     */
    public <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("反序列化 JSON 失败, type: {}", typeReference.getType(), e);
            return null;
        }
    }

    // ==================== Value 操作（String） ====================

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值，不存在时返回 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 获取对象（自动反序列化）
     *
     * @param key  键
     * @param clazz 目标类型
     * @param <T>  泛型
     * @return 对象，不存在或反序列化失败时返回 null
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String json = get(key);
        return fromJson(json, clazz);
    }

    /**
     * 获取泛型集合/复杂类型对象
     *
     * @param key          键
     * @param typeReference 类型引用
     * @param <T>          泛型
     * @return 对象，不存在或反序列化失败时返回 null
     */
    public <T> T getObject(String key, TypeReference<T> typeReference) {
        String json = get(key);
        return fromJson(json, typeReference);
    }

    /**
     * 设置字符串值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置对象（自动序列化）
     *
     * @param key  键
     * @param obj  对象
     */
    public void setObject(String key, Object obj) {
        String json = toJson(obj);
        if (json != null) {
            stringRedisTemplate.opsForValue().set(key, json);
        }
    }

    /**
     * 设置字符串值并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置对象并指定过期时间
     *
     * @param key     键
     * @param obj     对象
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void setObject(String key, Object obj, long timeout, TimeUnit unit) {
        String json = toJson(obj);
        if (json != null) {
            stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
        }
    }

    /**
     * 仅在键不存在时设置值（SET NX）
     *
     * @param key   键
     * @param value 值
     * @return true 设置成功，false 键已存在
     */
    public boolean setIfAbsent(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, value));
    }

    /**
     * 仅在键不存在时设置对象并指定过期时间（SET NX EX）
     *
     * @param key     键
     * @param obj     对象
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 设置成功，false 键已存在
     */
    public boolean setIfAbsent(String key, Object obj, long timeout, TimeUnit unit) {
        String json = toJson(obj);
        if (json == null) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, json, timeout, unit));
    }

    /**
     * 批量获取值
     *
     * @param keys 键集合
     * @return 值列表，不存在的键对应 null
     */
    public List<String> multiGet(Collection<String> keys) {
        return stringRedisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量设置值
     *
     * @param map 键值映射
     */
    public void multiSet(Map<String, String> map) {
        stringRedisTemplate.opsForValue().multiSet(map);
    }

    // ==================== 原子自增/自减 ====================

    /**
     * 原子自增（值 +1）
     *
     * @param key 键
     * @return 自增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 原子自增指定步长
     *
     * @param key   键
     * @param delta 步长（正数自增，负数自减）
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 原子自减（值 -1）
     *
     * @param key 键
     * @return 自减后的值
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 原子自减指定步长
     *
     * @param key   键
     * @param delta 步长（正数自减）
     * @return 自减后的值
     */
    public Long decrement(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== Hash 操作 ====================

    /**
     * 获取 Hash 中指定字段的值
     *
     * @param key   键
     * @param field 字段名
     * @return 值，不存在时返回 null
     */
    public String hGet(String key, String field) {
        return (String) stringRedisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取 Hash 中指定字段的对象（自动反序列化）
     *
     * @param key   键
     * @param field 字段名
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 对象，不存在或反序列化失败时返回 null
     */
    public <T> T hGetObject(String key, String field, Class<T> clazz) {
        String json = hGet(key, field);
        return fromJson(json, clazz);
    }

    /**
     * 设置 Hash 字段值
     *
     * @param key   键
     * @param field 字段名
     * @param value 值
     */
    public void hSet(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 设置 Hash 字段对象（自动序列化）
     *
     * @param key   键
     * @param field 字段名
     * @param obj   对象
     */
    public void hSetObject(String key, String field, Object obj) {
        String json = toJson(obj);
        if (json != null) {
            stringRedisTemplate.opsForHash().put(key, field, json);
        }
    }

    /**
     * 批量设置 Hash 字段
     *
     * @param key 键
     * @param map 字段值映射
     */
    public void hPutAll(String key, Map<String, String> map) {
        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 删除 Hash 字段
     *
     * @param key    键
     * @param fields 字段名（可变参数）
     */
    public void hDelete(String key, String... fields) {
        stringRedisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    /**
     * 判断 Hash 中是否存在指定字段
     *
     * @param key   键
     * @param field 字段名
     * @return true 存在，false 不存在
     */
    public boolean hExists(String key, String field) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForHash().hasKey(key, field));
    }

    /**
     * 获取 Hash 所有字段-值映射
     *
     * @param key 键
     * @return 字段-值映射
     */
    public Map<String, String> hGetAll(String key) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>(entries.size());
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            result.put((String) entry.getKey(), (String) entry.getValue());
        }
        return result;
    }

    /**
     * 获取 Hash 所有字段-对象映射（自动反序列化）
     *
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 字段-对象映射，反序列化失败时跳过该字段
     */
    public <T> Map<String, T> hGetAllObjects(String key, Class<T> clazz) {
        Map<String, String> entries = hGetAll(key);
        if (entries.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, T> result = new HashMap<>(entries.size());
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            T obj = fromJson(entry.getValue(), clazz);
            if (obj != null) {
                result.put(entry.getKey(), obj);
            }
        }
        return result;
    }

    /**
     * 获取 Hash 的字段数量
     *
     * @param key 键
     * @return 字段数量
     */
    public Long hSize(String key) {
        return stringRedisTemplate.opsForHash().size(key);
    }

    /**
     * 获取 Hash 所有字段名
     *
     * @param key 键
     * @return 字段名集合
     */
    public Set<String> hKeys(String key) {
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(key);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        return keys.stream().map(Object::toString).collect(Collectors.toSet());
    }

    /**
     * 获取 Hash 所有值
     *
     * @param key 键
     * @return 值列表
     */
    public List<String> hValues(String key) {
        List<Object> values = stringRedisTemplate.opsForHash().values(key);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream().map(v -> (String) v).collect(Collectors.toList());
    }

    /**
     * Hash 字段原子自增
     *
     * @param key   键
     * @param field 字段名
     * @param delta 步长（正数自增，负数自减）
     * @return 自增后的值
     */
    public Long hIncrement(String key, String field, long delta) {
        return stringRedisTemplate.opsForHash().increment(key, field, delta);
    }

    // ==================== List 操作 ====================

    /**
     * 从左侧推入元素（LPUSH）
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public Long lPush(String key, String value) {
        return stringRedisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 从左侧批量推入元素（LPUSH ...）
     *
     * @param key    键
     * @param values 值集合
     * @return 列表长度
     */
    public Long lPushAll(String key, Collection<String> values) {
        return stringRedisTemplate.opsForList().leftPushAll(key, values);
    }

    /**
     * 从右侧推入元素（RPUSH）
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public Long rPush(String key, String value) {
        return stringRedisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 从右侧批量推入元素（RPUSH ...）
     *
     * @param key    键
     * @param values 值集合
     * @return 列表长度
     */
    public Long rPushAll(String key, Collection<String> values) {
        return stringRedisTemplate.opsForList().rightPushAll(key, values);
    }

    /**
     * 从左侧弹出元素（LPOP）
     *
     * @param key 键
     * @return 弹出的元素，列表为空时返回 null
     */
    public String lPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    /**
     * 从右侧弹出元素（RPOP）
     *
     * @param key 键
     * @return 弹出的元素，列表为空时返回 null
     */
    public String rPop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取列表指定范围元素（LRANGE）
     *
     * @param key   键
     * @param start 起始索引（0 开始）
     * @param end   结束索引（-1 表示全部）
     * @return 元素列表
     */
    public List<String> lRange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 截取列表（LTRIM），只保留指定范围内的元素
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     */
    public void lTrim(String key, long start, long end) {
        stringRedisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 列表长度
     */
    public Long lSize(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    /**
     * 获取列表指定索引的元素
     *
     * @param key   键
     * @param index 索引
     * @return 元素，越界时返回 null
     */
    public String lIndex(String key, long index) {
        return stringRedisTemplate.opsForList().index(key, index);
    }

    /**
     * 设置列表指定索引的值
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    public void lSet(String key, long index, String value) {
        stringRedisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 移除列表中指定值的元素
     *
     * @param key   键
     * @param count 移除数量（0 表示所有）
     * @param value 要移除的值
     * @return 实际移除的数量
     */
    public Long lRemove(String key, long count, String value) {
        return stringRedisTemplate.opsForList().remove(key, count, value);
    }

    // ==================== Set 操作 ====================

    /**
     * 向集合添加元素
     *
     * @param key    键
     * @param values 值（可变参数）
     * @return 添加成功的元素数量
     */
    public Long sAdd(String key, String... values) {
        return stringRedisTemplate.opsForSet().add(key, values);
    }

    /**
     * 移除集合中的元素
     *
     * @param key    键
     * @param values 值（可变参数）
     * @return 移除成功的元素数量
     */
    public Long sRemove(String key, String... values) {
        return stringRedisTemplate.opsForSet().remove(key, (Object[]) values);
    }

    /**
     * 获取集合所有元素
     *
     * @param key 键
     * @return 元素集合
     */
    public Set<String> sMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    /**
     * 判断元素是否在集合中
     *
     * @param key   键
     * @param value 值
     * @return true 存在，false 不存在
     */
    public boolean sIsMember(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * 获取集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long sSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    /**
     * 随机弹出集合中的一个元素
     *
     * @param key 键
     * @return 弹出的元素，集合为空时返回 null
     */
    public String sPop(String key) {
        return stringRedisTemplate.opsForSet().pop(key);
    }

    /**
     * 随机弹出集合中的多个元素
     *
     * @param key   键
     * @param count 数量
     * @return 弹出的元素列表
     */
    public List<String> sPop(String key, long count) {
        return stringRedisTemplate.opsForSet().pop(key, count);
    }

    /**
     * 获取多个集合的交集
     *
     * @param key      第一个集合
     * @param otherKeys 其他集合
     * @return 交集元素集合
     */
    public Set<String> sIntersect(String key, Collection<String> otherKeys) {
        return stringRedisTemplate.opsForSet().intersect(key, otherKeys);
    }

    /**
     * 获取多个集合的并集
     *
     * @param key      第一个集合
     * @param otherKeys 其他集合
     * @return 并集元素集合
     */
    public Set<String> sUnion(String key, Collection<String> otherKeys) {
        return stringRedisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * 获取多个集合的差集
     *
     * @param key      第一个集合
     * @param otherKeys 其他集合
     * @return 差集元素集合
     */
    public Set<String> sDifference(String key, Collection<String> otherKeys) {
        return stringRedisTemplate.opsForSet().difference(key, otherKeys);
    }

    // ==================== ZSet 操作（有序集合） ====================

    /**
     * 向有序集合添加元素
     *
     * @param key   键
     * @param value 元素值
     * @param score 分数
     * @return true 添加成功，false 添加失败
     */
    public boolean zAdd(String key, String value, double score) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForZSet().add(key, value, score));
    }

    /**
     * 移除有序集合中的元素
     *
     * @param key    键
     * @param values 元素值（可变参数）
     * @return 移除成功的元素数量
     */
    public Long zRemove(String key, String... values) {
        return stringRedisTemplate.opsForZSet().remove(key, (Object[]) values);
    }

    /**
     * 获取有序集合指定分数范围内的元素（按分数升序）
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 获取有序集合指定排名范围的元素（按分数升序）
     *
     * @param key   键
     * @param start 起始排名（0 开始）
     * @param end   结束排名（-1 表示全部）
     * @return 元素集合
     */
    public Set<String> zRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取有序集合指定排名范围的元素（按分数降序）
     *
     * @param key   键
     * @param start 起始排名
     * @param end   结束排名
     * @return 元素集合
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取元素的分数
     *
     * @param key   键
     * @param value 元素值
     * @return 分数，元素不存在时返回 null
     */
    public Double zScore(String key, String value) {
        return stringRedisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 获取元素的排名（按分数升序，0 为最小）
     *
     * @param key   键
     * @param value 元素值
     * @return 排名，元素不存在时返回 null
     */
    public Long zRank(String key, String value) {
        return stringRedisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取元素的排名（按分数降序，0 为最大）
     *
     * @param key   键
     * @param value 元素值
     * @return 排名，元素不存在时返回 null
     */
    public Long zReverseRank(String key, String value) {
        return stringRedisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 获取有序集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long zSize(String key) {
        return stringRedisTemplate.opsForZSet().size(key);
    }

    /**
     * 移除有序集合中指定分数范围内的元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 移除的元素数量
     */
    public Long zRemoveByScore(String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    // ==================== Key 操作 ====================

    /**
     * 删除键
     *
     * @param key 键
     * @return true 删除成功，false 键不存在
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除成功的数量
     */
    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true 设置成功，false 键不存在
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
    }

    /**
     * 设置过期时间点
     *
     * @param key  键
     * @param date 过期时间点
     * @return true 设置成功，false 键不存在
     */
    public boolean expireAt(String key, Date date) {
        return Boolean.TRUE.equals(stringRedisTemplate.expireAt(key, date));
    }

    /**
     * 获取键的剩余过期时间
     *
     * @param key  键
     * @param unit 时间单位
     * @return 剩余过期时间，-1 永不过期，-2 键不存在
     */
    public Long getExpire(String key, TimeUnit unit) {
        return stringRedisTemplate.getExpire(key, unit);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return true 存在，false 不存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 移除过期时间（持久化）
     *
     * @param key 键
     * @return true 成功，false 键不存在或已过期
     */
    public boolean persist(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.persist(key));
    }

    // ==================== 分布式锁（简单实现） ====================

    /**
     * 尝试获取分布式锁（SET NX EX）
     * <p>
     * 使用默认锁超时时间 30 秒
     *
     * @param key      锁键
     * @param requestId 请求标识（建议使用 UUID，用于解锁时校验）
     * @return true 获取成功，false 获取失败
     */
    public boolean tryLock(String key, String requestId) {
        return setIfAbsent(key, requestId, 30, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取分布式锁（指定超时时间）
     *
     * @param key       锁键
     * @param requestId 请求标识
     * @param timeout   锁超时时间
     * @param unit      时间单位
     * @return true 获取成功，false 获取失败
     */
    public boolean tryLock(String key, String requestId, long timeout, TimeUnit unit) {
        return setIfAbsent(key, requestId, timeout, unit);
    }

    /**
     * 释放分布式锁（Lua 脚本保证原子性）
     * <p>
     * 仅当 requestId 匹配时才释放，防止误删其他线程的锁
     *
     * @param key       锁键
     * @param requestId 请求标识
     * @return true 释放成功，false 释放失败（锁不属于当前线程或已过期）
     */
    public boolean unlock(String key, String requestId) {
        String luaScript = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(key), requestId);
        return Long.valueOf(1L).equals(result);
    }

    // ==================== 其他操作 ====================

    /**
     * 获取 RedisTemplate，用于直接调用底层 API
     *
     * @return StringRedisTemplate 实例
     */
    public StringRedisTemplate getTemplate() {
        return stringRedisTemplate;
    }

    /**
     * 执行 Lua 脚本
     *
     * @param script Redis 脚本
     * @param keys   键列表
     * @param args   参数列表
     * @param <T>    返回值类型
     * @return 脚本执行结果
     */
    public <T> T execute(DefaultRedisScript<T> script, List<String> keys, Object... args) {
        return stringRedisTemplate.execute(script, keys, args);
    }
}