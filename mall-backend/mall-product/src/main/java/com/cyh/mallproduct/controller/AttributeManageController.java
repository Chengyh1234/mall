package com.cyh.mallproduct.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallproduct.dto.AttributeDto;
import com.cyh.mallproduct.dto.AttributeValueDto;
import com.cyh.mallproduct.service.AttributeManageService;
import com.cyh.mallproduct.vo.AttrValueVo;
import com.cyh.mallproduct.vo.AttributeVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性管理控制器（后台管理用） 已处理响应
 *
 * 提供属性及属性值的增删改查功能
 * 权限：仅限 SUPER_ADMIN 角色访问
 *
 * API 前缀：/attribute/manage
 */
@Slf4j
@RestController
@RequestMapping("/attribute/manage")
@RequiredArgsConstructor
public class AttributeManageController {

    private final AttributeManageService attributeManageService;

    // ==================== 属性管理 ====================

    /**
     * 新增属性
     *
     * 权限：SUPER_ADMIN
     *
     * @param dto 属性信息（name、attrType必填，sort可选）
     * @return 新增结果（含属性ID）
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> addAttribute(@RequestBody @Validated AttributeDto dto) {
        Long id = attributeManageService.addAttribute(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    /**
     * 修改属性
     *
     * 权限：SUPER_ADMIN
     * 规则：如果该属性下已有属性值，则禁止修改属性类型（attrType），但可以修改名称和排序
     *
     * @param id  属性ID
     * @param dto 新的属性信息
     * @return 修改结果
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateAttribute(@PathVariable Long id, @RequestBody @Validated AttributeDto dto) {
        boolean success = attributeManageService.updateAttribute(id, dto);
        return success ? Result.success("修改成功", null) : Result.error("修改失败");
    }

    /**
     * 删除属性
     *
     * 权限：SUPER_ADMIN
     * 规则：如果该属性下存在属性值，则禁止删除
     *
     * @param id 属性ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteAttribute(@PathVariable Long id) {
        boolean success = attributeManageService.deleteAttribute(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 查询全部属性列表（含属性值）
     *
     * 权限：SUPER_ADMIN
     *
     * @return 属性列表（每个属性含其下的属性值）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<AttributeVo>> listAttributes() {
        List<AttributeVo> list = attributeManageService.listAllAttributes();
        return Result.success(list);
    }

    /**
     * 查询单个属性详情（含属性值）
     *
     * 权限：SUPER_ADMIN
     *
     * @param id 属性ID
     * @return 属性详情（含属性值列表）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<AttributeVo> getAttribute(@PathVariable Long id) {
        AttributeVo vo = attributeManageService.getAttributeDetail(id);
        return Result.success(vo);
    }

    // ==================== 属性值管理 ====================

    /**
     * 在指定属性下新增属性值
     *
     * 权限：SUPER_ADMIN
     *
     * @param dto 属性值信息（attrId、value必填，imageUrl、sort可选）
     * @return 新增结果（含属性值ID）
     */
    @PostMapping("/value/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> addAttributeValue(@RequestBody @Validated AttributeValueDto dto) {
        Long id = attributeManageService.addAttributeValue(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success("新增成功", data);
    }

    /**
     * 修改属性值
     *
     * 权限：SUPER_ADMIN
     * 规则：如果该属性值已被 SPU 或 SKU 引用，则禁止修改
     *
     * @param id  属性值ID
     * @param dto 新的属性值信息
     * @return 修改结果
     */
    @PutMapping("/value/update/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateAttributeValue(@PathVariable Long id, @RequestBody @Validated AttributeValueDto dto) {
        boolean success = attributeManageService.updateAttributeValue(id, dto);
        return success ? Result.success("修改成功", null) : Result.error("修改失败");
    }

    /**
     * 删除属性值
     *
     * 权限：SUPER_ADMIN
     * 规则：如果该属性值已被 SPU 或 SKU 引用，则禁止删除
     *
     * @param id 属性值ID
     * @return 删除结果
     */
    @DeleteMapping("/value/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteAttributeValue(@PathVariable Long id) {
        boolean success = attributeManageService.deleteAttributeValue(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 根据属性ID查询其下的所有属性值
     *
     * 权限：SUPER_ADMIN
     *
     * @param attrId 属性ID
     * @return 属性值列表
     */
    @GetMapping("/value/list/{attrId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<AttrValueVo>> listAttributeValues(@PathVariable Long attrId) {
        List<AttrValueVo> list = attributeManageService.listAttributeValues(attrId);
        return Result.success(list);
    }
}