package com.cyh.mallstore.controller;

import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallstore.entity.Banner;
import com.cyh.mallstore.service.BannerService;
import com.cyh.mallstore.service.FileService;
import com.cyh.mallstore.vo.BannerAdminVo;
import com.cyh.mallstore.vo.BannerVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图管理控制器
 * 提供轮播图的增删改查、启用/禁用、前端活跃列表查询等功能
 * 管理端接口需 SUPER_ADMIN 权限，前端活跃列表公开访问
 */
@Slf4j
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;
    private final FileService fileService;

    /**
     * 新增轮播图（管理端，需 SUPER_ADMIN 权限）
     * 上传图片到 banners 目录，设置默认排序为 0，状态为启用
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> add(@RequestParam("title") String title,
                                           @RequestParam(value = "linkUrl", required = false) String linkUrl,
                                           @RequestParam("imageFile") MultipartFile imageFile) {
        if (!StringUtils.hasText(title)) {
            return Result.error("标题不能为空");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            return Result.error("图片文件不能为空");
        }
        Map<String, String> uploadResult = fileService.uploadImage(imageFile, FileConstants.BANNERS);
        if (uploadResult == null) {
            return Result.error("图片上传失败");
        }
        Banner banner = new Banner();
        banner.setTitle(title);
        banner.setImageUrl(uploadResult.get("relativePath"));
        banner.setLinkUrl(StringUtils.hasText(linkUrl) ? linkUrl : null);
        banner.setSort(0);
        banner.setStatus(1);
        Long id = bannerService.add(banner);
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("imageUrl", "/uploads/images/banners/" + uploadResult.get("relativePath"));
            return Result.success("新增成功", data);
        }
        return Result.error("新增失败");
    }

    /**
     * 更新轮播图信息（管理端，需 SUPER_ADMIN 权限）
     * 支持替换图片文件，更新后自动清除缓存
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> update(@RequestParam("id") Long id,
                                              @RequestParam("title") String title,
                                              @RequestParam(value = "linkUrl", required = false) String linkUrl,
                                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        if (id == null) {
            return Result.error("轮播图ID不能为空");
        }
        if (!StringUtils.hasText(title)) {
            return Result.error("标题不能为空");
        }
        Banner existingBanner = bannerService.getById(id);
        if (existingBanner == null) {
            return Result.error("轮播图不存在");
        }
        Banner banner = new Banner();
        banner.setId(id);
        banner.setTitle(title);
        banner.setLinkUrl(StringUtils.hasText(linkUrl) ? linkUrl : existingBanner.getLinkUrl());

        if (imageFile != null && !imageFile.isEmpty()) {
            String oldImageUrl = existingBanner.getImageUrl();
            Map<String, String> uploadResult = fileService.uploadImage(imageFile, FileConstants.BANNERS);
            if (uploadResult == null) {
                return Result.error("图片上传失败");
            }
            banner.setImageUrl(uploadResult.get("relativePath"));
            boolean updated = bannerService.update(banner);
            if (updated) {
                if (StringUtils.hasText(oldImageUrl)) {
                    fileService.deleteFile(oldImageUrl, FileConstants.BANNERS);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("id", id);
                data.put("imageUrl", "/uploads/images/banners/" + uploadResult.get("relativePath"));
                return Result.success("更新成功", data);
            }
        } else {
            banner.setImageUrl(existingBanner.getImageUrl());
            boolean updated = bannerService.update(banner);
            if (updated) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", id);
                return Result.success("更新成功", data);
            }
        }
        return Result.error("更新失败");
    }

    /**
     * 删除轮播图（管理端，需 SUPER_ADMIN 权限）
     * 删除后自动清除缓存并删除本地图片文件
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        if (id == null) {
            return Result.error("轮播图ID不能为空");
        }
        Banner existing = bannerService.getById(id);
        if (existing == null) {
            return Result.error("轮播图不存在");
        }
        boolean success = bannerService.delete(id);
        if (success) {
            if (StringUtils.hasText(existing.getImageUrl())) {
                fileService.deleteFile(existing.getImageUrl(), FileConstants.BANNERS);
            }
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 启用轮播图（管理端，需 SUPER_ADMIN 权限）
     * 将状态设为 1，启用后出现在前端轮播列表中
     */
    @PutMapping("/enable/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> enable(@PathVariable Long id) {
        if (id == null) {
            return Result.error("轮播图ID不能为空");
        }
        boolean success = bannerService.updateStatus(id, 1);
        if (success) {
            return Result.success("轮播图已启用", null);
        }
        return Result.error("启用失败");
    }

    /**
     * 禁用轮播图（管理端，需 SUPER_ADMIN 权限）
     * 将状态设为 0，禁用后不会出现在前端轮播列表中
     */
    @PutMapping("/disable/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> disable(@PathVariable Long id) {
        if (id == null) {
            return Result.error("轮播图ID不能为空");
        }
        boolean success = bannerService.updateStatus(id, 0);
        if (success) {
            return Result.success("轮播图已禁用", null);
        }
        return Result.error("禁用失败");
    }

    /**
     * 查询轮播图列表（管理端，按状态筛选）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<BannerAdminVo>> getList(@RequestParam(value = "status", required = false) Integer status) {
        List<Banner> list = bannerService.getList(status);
        List<BannerAdminVo> voList = list.stream().map(BannerAdminVo::fromBanner).collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 查询启用的轮播图列表（前端，公开访问，优先从缓存读取）
     */
    @GetMapping("/active")
    public Result<List<BannerVo>> getActiveList() {
        List<Banner> banners = bannerService.getActiveList();
        List<BannerVo> voList = banners.stream().map(BannerVo::fromBanner).collect(Collectors.toList());
        return Result.success(voList);
    }
}