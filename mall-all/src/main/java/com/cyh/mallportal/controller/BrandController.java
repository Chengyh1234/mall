package com.cyh.mallportal.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.BrandDto;
import com.cyh.mallportal.entity.Brand;
import com.cyh.mallportal.service.BrandService;
import com.cyh.mallportal.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 品牌管理控制器
 * 提供品牌的增删改查功能及Logo上传功能
 */
@Slf4j
@RestController
@RequestMapping("/brand")
public class BrandController {

    /**
     * 品牌服务接口
     */
    @Autowired
    private BrandService brandService;

    @Autowired
    private FileService fileService;

    /**
     * Logo上传路径配置
     */
    @Value("${file.upload.logo-path:./uploads/logos}")
    private String logoPath;

    /**
     * 新增品牌（支持form-data格式上传）
     *
     * @param brandDtoString 品牌信息的JSON字符串
     * @param logoFile 上传的Logo文件（非必填）
     * @return 新增结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
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
    @PreAuthorize("hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
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
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
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
    public Result<Brand> getById(@PathVariable Long id) {
        // 参数校验
        if (id == null) {
            return Result.error("品牌ID不能为空");
        }

        // 调用service查询品牌详情
        Brand brand = brandService.getById(id);
        if (brand != null) {
            return Result.success(brand);
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
    public Result<List<Brand>> getList(Brand brand) {
        // 调用service查询品牌列表
        List<Brand> list = brandService.getList(brand);
        return Result.success(list);
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
        data.put("list", pageResult.getRecords());
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
    public Result<List<Brand>> getByStatus(@PathVariable Integer status) {
        // 参数校验
        if (status == null) {
            return Result.error("状态不能为空");
        }

        // 调用service查询品牌列表
        List<Brand> list = brandService.getByStatus(status);
        return Result.success(list);
    }

    /**
     * 根据品牌名称模糊查询品牌列表
     *
     * @param name 品牌名称（支持模糊匹配）
     * @return 品牌列表
     */
    @GetMapping("/search")
    public Result<List<Brand>> getByNameLike(@RequestParam String name) {
        // 参数校验
        if (!StringUtils.hasText(name)) {
            return Result.error("品牌名称不能为空");
        }

        // 调用service查询品牌列表
        List<Brand> list = brandService.getByNameLike(name);
        return Result.success(list);
    }

    /**
     * 按排序号获取品牌列表
     *
     * @return 品牌列表，按排序号升序排列
     */
    @GetMapping("/sort")
    public Result<List<Brand>> getBySort() {
        // 调用service查询品牌列表
        List<Brand> list = brandService.getBySort();
        return Result.success(list);
    }

    /**
     * 上传品牌Logo（使用公共FileService）
     */
    private Map<String, String> uploadLogo(MultipartFile file) {
        Map<String, String> result = fileService.uploadImage(file, "brands");
        if (result != null) {
            Map<String, String> response = new HashMap<>();
            response.put("logo", result.get("relativePath"));
            response.put("logoUrl", "/uploads/images/brands/" + result.get("relativePath"));
            return response;
        }
        return null;
    }

    private void deleteLogoFile(String logo) {
        fileService.deleteFile(logo, "brands");
    }
}
