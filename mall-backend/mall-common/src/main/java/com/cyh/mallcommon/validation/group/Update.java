package com.cyh.mallcommon.validation.group;

/**
 * 校验分组 - 修改场景
 * <p>
 * 使用场景：PUT 修改接口。
 * 分组规则：
 * <ul>
 *   <li>主键 id：{@code @NotNull(groups = Update.class)} —— 修改时必须指定主键</li>
 *   <li>新增/修改均必填的字段：{@code groups = {Create.class, Update.class}}</li>
 *   <li>仅修改时必填的字段：{@code groups = Update.class}</li>
 * </ul>
 */
public interface Update {
}