package com.cyh.mallproduct.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallproduct.dto.BrandDto;
import com.cyh.mallproduct.entity.Brand;
import com.cyh.mallproduct.service.BrandService;
import com.cyh.mallproduct.service.FileService;
import com.cyh.mallproduct.vo.BrandVo;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 品牌管理控制器 已处理响应
 * 提供品牌的增删改查功能及Logo上传功能
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/brand")
public class BrandController {

    private final BrandService brandService;
    private final FileService fileService;

    /**
     * 新增品牌（支持form-data格式上传）
     *
     * @param brandDtoString 品牌信息的JSON字符串
     * @param logoFile 上传的Logo文件（非必填）
     * @return 新增结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> add(@RequestPart(value = "brandDto") String brandDtoString,
                                           @RequestPart(value = "logoFile", required = false) MultipartFile logoFile) {
        // 解析品牌DTO
        BrandDto brandDto = JSON.parseObject(brandDtoString, BrandDto.class);

        // 参数校验
        if (!StringUtils.hasText(brandDto.getName())) {
            return Result.error("品牌名称不能为空");
        }

        // 创建Brand实体
        Brand brand = new Brand();
        brand.setName(brandDto.getName());
        brand.setDescription(brandDto.getDescription());
        brand.setWebsite(brandDto.getWebsite());
        brand.setSort(brandDto.getSort() != null ? brandDto.getSort() : 0);
        brand.setStatus(brandDto.getStatus() != null ? brandDto.getStatus() : 1);

        // 处理Logo上传
        if (logoFile != null && !logoFile.isEmpty()) {
            Map<String, String> logoInfo = uploadLogo(logoFile);
            if (logoInfo != null) {
                brand.setLogo(logoInfo.get("logo"));
            } else {
                return Result.error("Logo上传失败");
            }
        }

        // 调用service新增品牌
        Long id = brandService.add(brand);
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("logo", brand.getLogo());
            return Result.success("新增成功", data);
        }
        return Result.error("新增失败");
    }

    /**
     * 删除品牌（软删除）
     *
     * @param id 品牌ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        // 参数校验
        if (id == null) {
            return Result.error("品牌ID不能为空");
        }

        // 调用service删除品牌
        boolean success = brandService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 更新品牌信息（支持form-data格式上传）
     *
     * @param brandDtoString 品牌信息的JSON字符串
     * @param logoFile 上传的Logo文件（非必填）
     * @return 更新结果
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> update(@RequestPart(value = "brandDto") String brandDtoString,
                                               @RequestPart(value = "logoFile", required = false) MultipartFile logoFile) {
        // 解析品牌DTO
        BrandDto brandDto = JSON.parseObject(brandDtoString, BrandDto.class);

        // 参数校验
        if (brandDto.getId() == null) {
            return Result.error("品牌ID不能为空");
        }
        if (!StringUtils.hasText(brandDto.getName())) {
            return Result.error("品牌名称不能为空");
        }

        // 查询原有品牌信息
        Brand existingBrand = brandService.getById(brandDto.getId());
        if (existingBrand == null) {
            return Result.error("品牌不存在");
        }

        // 创建Brand实体并设置更新后的值
        Brand brand = new Brand();
        brand.setId(brandDto.getId());
        brand.setName(brandDto.getName());
        brand.setDescription(brandDto.getDescription());
        brand.setWebsite(brandDto.getWebsite());
        brand.setSort(brandDto.getSort() != null ? brandDto.getSort() : existingBrand.getSort());
        brand.setStatus(brandDto.getStatus() != null ? brandDto.getStatus() : existingBrand.getStatus());

        // 处理新上传的Logo
        if (logoFile != null && !logoFile.isEmpty()) {
            // 删除旧Logo
            if (StringUtils.hasText(existingBrand.getLogo())) {
                deleteLogoFile(existingBrand.getLogo());
            }
            // 上传新Logo
            Map<String, String> logoInfo = uploadLogo(logoFile);
            if (logoInfo != null) {
                brand.setLogo(logoInfo.get("logo"));
            } else {
                return Result.error("Logo上传失败");
            }
        } else {
            // 保留原有Logo
            brand.setLogo(existingBrand.getLogo());
        }

        // 调用service更新品牌
        boolean success = brandService.update(brand);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", brand.getId());
            data.put("logo", brand.getLogo());
            return Result.success("更新成功", data);
        }
        return Result.error("更新失败");
    }

    /**
     * 根据ID获取品牌详情
     *
     * @param id 品牌ID
     * @return 品牌详情
     */
    @GetMapping("/detail/{id}")
    public Result<BrandVo> getById(@PathVariable Long id) {
        // 参数校验
        if (id == null) {
            return Result.error("品牌ID不能为空");
        }

        // 调用service查询品牌详情
        Brand brand = brandService.getById(id);
        if (brand != null) {
            return Result.success(BrandVo.fromBrand(brand));
        }
        return Result.error("品牌不存在");
    }

    /**
     * 获取品牌列表
     *
     * @param brand 查询条件（支持id、name、status）
     * @return 品牌列表
     */
    @GetMapping("/list")
    public Result<List<BrandVo>> getList(Brand brand) {
        // 调用service查询品牌列表
        List<Brand> list = brandService.getList(brand);
        List<BrandVo> voList = list.stream()
                .map(BrandVo::fromBrand)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 分页获取品牌列表
     *
     * @param brand 查询条件（支持id、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 品牌分页列表
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getPage(Brand brand,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用service分页查询品牌列表
        IPage<Brand> pageResult = brandService.getPage(brand, page, pageSize);

        // 构建返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords().stream()
                .map(BrandVo::fromBrand)
                .collect(Collectors.toList()));
        data.put("total", pageResult.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("pages", pageResult.getPages());

        return Result.success(data);
    }

    /**
     * 根据状态获取品牌列表
     *
     * @param status 状态（1-启用 0-禁用）
     * @return 品牌列表
     */
    @GetMapping("/status/{status}")
    public Result<List<BrandVo>> getByStatus(@PathVariable Integer status) {
        // 参数校验
        if (status == null) {
            return Result.error("状态不能为空");
        }

        // 调用service查询品牌列表
        List<Brand> list = brandService.getByStatus(status);
        List<BrandVo> voList = list.stream()
                .map(BrandVo::fromBrand)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 根据品牌名称模糊查询品牌列表
     *
     * @param name 品牌名称（支持模糊匹配）
     * @return 品牌列表
     */
    @GetMapping("/search")
    public Result<List<BrandVo>> getByNameLike(@RequestParam String name) {
        // 参数校验
        if (!StringUtils.hasText(name)) {
            return Result.error("品牌名称不能为空");
        }

        // 调用service查询品牌列表
        List<Brand> list = brandService.getByNameLike(name);
        List<BrandVo> voList = list.stream()
                .map(BrandVo::fromBrand)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 按排序号获取品牌列表
     *
     * @return 品牌列表，按排序号升序排列
     */
    @GetMapping("/sort")
    public Result<List<BrandVo>> getBySort() {
        // 调用service查询品牌列表
        List<Brand> list = brandService.getBySort();
        List<BrandVo> voList = list.stream()
                .map(BrandVo::fromBrand)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 上传品牌Logo（使用公共FileService）
     */
    private Map<String, String> uploadLogo(MultipartFile file) {
        Map<String, String> result = fileService.uploadImage(file, FileConstants.BRAND_LOGO);
        if (result != null) {
            Map<String, String> response = new HashMap<>();
            response.put("logo", result.get("relativePath"));
            response.put("logoUrl", "/uploads/images/brand/logo/" + result.get("relativePath"));
            return response;
        }
        return null;
    }

    private void deleteLogoFile(String logo) {
        fileService.deleteFile(logo, FileConstants.BRAND_LOGO);
    }
}