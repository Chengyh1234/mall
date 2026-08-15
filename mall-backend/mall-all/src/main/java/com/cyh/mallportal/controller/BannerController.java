package com.cyh.mallportal.controller;

import com.cyh.mallportal.entity.Banner;
import com.cyh.mallportal.service.BannerService;
import com.cyh.mallportal.service.FileService;
import com.cyh.mallportal.vo.BannerAdminVo;
import com.cyh.mallportal.vo.BannerVo;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.constant.FileConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图管理控制器 已经处理响应
 * 提供轮播图的增删改查和状态管理功能
 *
 * 权限说明：
 * - 管理端接口（增/删/改/查全部）仅限 SUPER_ADMIN、ADMIN 角色访问
 * - 首页展示接口（/banner/active）公开访问，无需登录
 */
@Slf4j
@RestController
@RequestMapping("/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private FileService fileService;

    /**
     * 新增轮播图
     *
     * 权限：SUPER_ADMIN、ADMIN
     *
     * @param title     轮播图标题
     * @param linkUrl   跳转链接（可选）
     * @param imageFile 轮播图图片文件
     * @return 新增结果
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
     * 编辑轮播图（可选更换图片）
     *
     * 权限：SUPER_ADMIN、ADMIN
     *
     * @param id        轮播图ID
     * @param title     轮播图标题
     * @param linkUrl   跳转链接（可选）
     * @param imageFile 轮播图图片文件（可选，不传则保留原图）
     * @return 更新结果
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
     * 删除轮播图（物理删除，同时删除图片文件）
     *
     * 权限：SUPER_ADMIN、ADMIN
     *
     * @param id 轮播图ID
     * @return 删除结果
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
     * 启用轮播图
     *
     * 权限：SUPER_ADMIN、ADMIN
     *
     * @param id 轮播图ID
     * @return 操作结果
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
     * 禁用轮播图
     *
     * 权限：SUPER_ADMIN、ADMIN
     *
     * @param id 轮播图ID
     * @return 操作结果
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
     * 获取轮播图列表（管理后台用，支持按状态筛选）
     *
     * 权限：SUPER_ADMIN、ADMIN
     * 可查看所有状态（启用+禁用）的轮播图
     *
     * @param status 状态（可选，传null查全部）
     * @return 轮播图列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<BannerAdminVo>> getList(@RequestParam(value = "status", required = false) Integer status) {
        List<Banner> list = bannerService.getList(status);
        List<BannerAdminVo> voList = list.stream()
                .map(BannerAdminVo::fromBanner)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取启用的轮播图列表（首页展示用，走Redis缓存）
     *
     * 公开接口，无需登录，仅返回启用状态（status=1）的轮播图
     *
     * @return 启用的轮播图列表
     */
    @GetMapping("/active")
    public Result<List<BannerVo>> getActiveList() {
        List<Banner> banners = bannerService.getActiveList();
        List<BannerVo> voList = banners.stream()
                .map(BannerVo::fromBanner)
                .collect(Collectors.toList());
        return Result.success(voList);
    }
}