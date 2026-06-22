package com.cyh.mallcommon.validation.group;

/**
 * 校验分组 - 新增场景
 * <p>
 * 使用场景：POST 新增接口。
 * 分组规则：
 * <ul>
 *   <li>主键 id：不校验（新增时由数据库自增或自动生成）</li>
 *   <li>必填业务字段：标注 {@code groups = Create.class}</li>
 *   <li>非必填字段：不标注任何分组（所有场景均不强制校验）</li>
 * </ul>
 */
public interface Create {
}