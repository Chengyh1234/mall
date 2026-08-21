package com.cyh.mallproduct.controller;

import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallproduct.dto.CategoryDto;
import com.cyh.mallproduct.entity.Category;
import com.cyh.mallproduct.service.CategoryService;
import com.cyh.mallproduct.service.FileService;
import com.cyh.mallproduct.vo.CategoryTreeVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类管理控制器 已处理响应
 * 提供分类的增删改查及树形结构查询功能
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;
    private final FileService fileService;

    /**
     * 新增分类（支持图标上传）
     *
     * @param categoryDtoString 分类信息的JSON字符串
     * @param iconFile 上传的图标文件（非必填）
     * @return 新增结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> add(@RequestPart(value = "categoryDto") String categoryDtoString,
                                           @RequestPart(value = "iconFile", required = false) MultipartFile iconFile) {
        // 解析分类DTO
        CategoryDto categoryDto = JSON.parseObject(categoryDtoString, CategoryDto.class);

        // 参数校验
        if (!StringUtils.hasText(categoryDto.getName())) {
            return Result.error("分类名称不能为空");
        }

        // 创建Category实体
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setParentId(categoryDto.getParentId() != null ? categoryDto.getParentId() : 0L);
        category.setLevel(categoryDto.getLevel());
        category.setSort(categoryDto.getSort() != null ? categoryDto.getSort() : 0);
        category.setStatus(categoryDto.getStatus() != null ? categoryDto.getStatus() : 1);

        // 处理图标上传
        if (iconFile != null && !iconFile.isEmpty()) {
            Map<String, String> iconInfo = uploadIcon(iconFile);
            if (iconInfo != null) {
                category.setIcon(iconInfo.get("icon"));
            } else {
                return Result.error("图标上传失败");
            }
        }

        // 调用service新增分类
        Long id = categoryService.add(category);
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("level", category.getLevel());
            data.put("icon", category.getIcon());
            return Result.success("添加成功", data);
        }
        return Result.error("添加失败");
    }

    /**
     * 删除分类（级联删除子分类，同时删除图标）
     *
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        // 获取分类信息（包含图标）
        Category category = categoryService.getById(id);
        if (category != null && StringUtils.hasText(category.getIcon())) {
            // 删除图标文件
            deleteIconFile(category.getIcon());
        }

        // 调用service删除分类（级联删除子分类）
        boolean success = categoryService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 更新分类信息（支持图标上传）
     *
     * @param categoryDtoString 分类信息的JSON字符串
     * @param iconFile 上传的图标文件（非必填）
     * @return 更新结果
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> update(@RequestPart(value = "categoryDto") String categoryDtoString,
                                              @RequestPart(value = "iconFile", required = false) MultipartFile iconFile) {
        // 解析分类DTO
        CategoryDto categoryDto = JSON.parseObject(categoryDtoString, CategoryDto.class);

        // 参数校验
        if (categoryDto.getId() == null) {
            return Result.error("分类ID不能为空");
        }

        // 查询原有分类信息
        Category existingCategory = categoryService.getById(categoryDto.getId());
        if (existingCategory == null) {
            return Result.error("分类不存在");
        }

        // 创建Category实体并设置更新后的值
        Category category = new Category();
        category.setId(categoryDto.getId());
        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }
        if (categoryDto.getParentId() != null) {
            category.setParentId(categoryDto.getParentId());
        }
        if (categoryDto.getSort() != null) {
            category.setSort(categoryDto.getSort());
        }
        if (categoryDto.getStatus() != null) {
            category.setStatus(categoryDto.getStatus());
        }

        // 处理新上传的图标
        if (iconFile != null && !iconFile.isEmpty()) {
            // 删除旧图标
            if (StringUtils.hasText(existingCategory.getIcon())) {
                deleteIconFile(existingCategory.getIcon());
            }
            // 上传新图标
            Map<String, String> iconInfo = uploadIcon(iconFile);
            if (iconInfo != null) {
                category.setIcon(iconInfo.get("icon"));
            } else {
                return Result.error("图标上传失败");
            }
        } else {
            // 保留原有图标
            category.setIcon(existingCategory.getIcon());
        }

        // 调用service更新分类
        boolean success = categoryService.update(category);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", category.getId());
            data.put("icon", category.getIcon());
            return Result.success("更新成功", data);
        }
        return Result.error("更新失败");
    }

    /**
     * 获取分类树形结构（从根节点构建整棵树，结果缓存到 Redis）
     *
     * @return 树形结构
     */
    @GetMapping("/tree")
    public Result<List<CategoryTreeVo>> getTree() {
        List<CategoryTreeVo> tree = categoryService.getTreeWithChildren();
        return Result.success(tree);
    }

    /**
     * 上传分类图标（使用公共FileService）
     *
     * @param file 图标文件
     * @return 上传结果，包含图标路径
     */
    private Map<String, String> uploadIcon(MultipartFile file) {
        Map<String, String> result = fileService.uploadImage(file, FileConstants.CATEGORY_ICONS);
        if (result != null) {
            Map<String, String> response = new HashMap<>();
            response.put("icon", result.get("relativePath"));
            response.put("iconUrl", "/uploads/images/icons/" + result.get("relativePath"));
            return response;
        }
        return null;
    }

    /**
     * 删除分类图标文件（使用公共FileService）
     *
     * @param icon 图标路径（相对路径）
     */
    private void deleteIconFile(String icon) {
        fileService.deleteFile(icon, FileConstants.CATEGORY_ICONS);
    }
}