package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.CategoryAttributeBindDto;
import com.cyh.mallportal.dto.CategoryAttributeSortDto;
import com.cyh.mallportal.service.CategoryAttributeManageService;
import com.cyh.mallportal.vo.AvailableAttributeVo;
import com.cyh.mallportal.vo.CategoryAttributeBoundVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类-属性绑定管理控制器（后台管理用） 已处理响应
 *
 * 提供分类与属性之间的绑定关系管理功能
 * 权限：仅限 SUPER_ADMIN 角色访问
 *
 * API 前缀：/category/attribute/manage
 */
@Slf4j
@RestController
@RequestMapping("/category/attribute/manage")
@RequiredArgsConstructor
public class CategoryAttributeManageController {

    private final CategoryAttributeManageService categoryAttributeManageService;

    /**
     * 获取分类已绑定的属性列表
     *
     * 权限：SUPER_ADMIN
     * 返回列表包含每个属性的 SPU 引用状态（hasSpuUsage），
     * 前端可根据该字段展示锁定图标，提示超级管理员哪些属性无法解绑
     *
     * @param categoryId 分类ID
     * @return 已绑定的属性列表（含 SPU 引用状态）
     */
    @GetMapping("/bound/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<CategoryAttributeBoundVo>> getBoundAttributes(@PathVariable Long categoryId) {
        List<CategoryAttributeBoundVo> list = categoryAttributeManageService.getBoundAttributes(categoryId);
        return Result.success(list);
    }

    /**
     * 获取分类可绑定的属性列表
     *
     * 权限：SUPER_ADMIN
     * 返回尚未绑定到该分类的所有属性，供超级管理员选择绑定
     *
     * @param categoryId 分类ID
     * @return 未绑定的属性列表
     */
    @GetMapping("/available/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<AvailableAttributeVo>> getAvailableAttributes(@PathVariable Long categoryId) {
        List<AvailableAttributeVo> list = categoryAttributeManageService.getAvailableAttributes(categoryId);
        return Result.success(list);
    }

    /**
     * 绑定属性到分类
     *
     * 权限：SUPER_ADMIN
     *
     * @param dto 绑定信息（categoryId、attrId必填，sort可选）
     * @return 绑定结果（含绑定记录ID）
     */
    @PostMapping("/bind")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> bindAttribute(@RequestBody @Validated CategoryAttributeBindDto dto) {
        Long id = categoryAttributeManageService.bindAttribute(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("绑定成功", data);
    }

    /**
     * 修改绑定记录的排序
     *
     * 权限：SUPER_ADMIN
     * 排序调整不影响已有 SPU 数据，无需约束
     *
     * @param id  绑定记录ID
     * @param dto 新的排序信息
     * @return 修改结果
     */
    @PutMapping("/update-sort/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateSort(@PathVariable Long id, @RequestBody @Validated CategoryAttributeSortDto dto) {
        boolean success = categoryAttributeManageService.updateSort(id, dto);
        return success ? Result.success("排序修改成功", null) : Result.error("排序修改失败");
    }

    /**
     * 解绑属性
     *
     * 权限：SUPER_ADMIN
     * 规则：如果该分类下的 SPU 已使用了该属性，则禁止解绑
     *
     * @param id 绑定记录ID
     * @return 解绑结果
     */
    @DeleteMapping("/unbind/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> unbindAttribute(@PathVariable Long id) {
        boolean success = categoryAttributeManageService.unbindAttribute(id);
        return success ? Result.success("解绑成功", null) : Result.error("解绑失败");
    }
}