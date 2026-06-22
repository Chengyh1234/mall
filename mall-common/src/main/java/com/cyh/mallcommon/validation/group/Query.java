package com.cyh.mallcommon.validation.group;

/**
 * 校验分组 - 查询场景
 * <p>
 * 使用场景：GET 查询接口，当某些参数在查询时为必填时使用。
 * 分组规则：
 * <ul>
 *   <li>查询时必填的参数：{@code @NotNull(groups = Query.class)}</li>
 *   <li>大部分查询参数为可选，无需标注任何分组</li>
 * </ul>
 */
public interface Query {
}