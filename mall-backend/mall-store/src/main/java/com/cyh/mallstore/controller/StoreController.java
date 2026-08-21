package com.cyh.mallstore.controller;

import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallstore.dto.StoreDto;
import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.service.FileService;
import com.cyh.mallstore.service.StoreService;
import com.cyh.mallstore.vo.StoreAdminVo;
import com.cyh.mallstore.vo.StoreDetailVo;
import com.cyh.mallstore.vo.StoreSellerVo;
import com.cyh.mallstore.vo.StoreVo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 店铺管理控制器
 * 提供店铺的增删改查、详情查询、管理端分页查询等功能
 * 店铺图片上传委托 FileService 处理
 */
@Validated
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final FileService fileService;

    /**
     * 创建店铺（含 Logo 和横幅图片上传）
     * 接收 JSON 格式的店铺信息和 Multipart 图片文件，支持分组校验
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> add(@RequestPart(value = "storeDto") String storeDtoString,
                                           @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
                                           @RequestPart(value = "bannerFile", required = false) MultipartFile bannerFile) {
        StoreDto storeDto = JSON.parseObject(storeDtoString, StoreDto.class);
        Store store = new Store();
        store.setName(storeDto.getName());
        store.setDescription(storeDto.getDescription());
        store.setPhone(storeDto.getPhone());
        store.setAddress(storeDto.getAddress());
        store.setBusinessLicense(storeDto.getBusinessLicense());
        store.setStatus(storeDto.getStatus());
        store.setSort(storeDto.getSort());

        store.setSellerId(getCurrentUserId());

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        if (logoFile != null && !logoFile.isEmpty()) {
            Map<String, String> logoInfo = uploadLogo(logoFile);
            if (logoInfo != null) {
                store.setLogo(date + "/" + logoInfo.get("fileName"));
            }
        }
        if (bannerFile != null && !bannerFile.isEmpty()) {
            Map<String, String> bannerInfo = uploadBanner(bannerFile);
            if (bannerInfo != null) {
                store.setBanner(date + "/" + bannerInfo.get("fileName"));
            }
        }

        Long id = storeService.add(store);
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("logo", store.getLogo());
            data.put("banner", store.getBanner());
            return Result.success("添加成功", data);
        }
        return Result.error("添加失败");
    }

    /**
     * 更新店铺信息
     * 先校验当前用户是否为店铺所有者，支持替换 Logo/横幅图片并清理旧文件
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> update(@RequestPart(value = "storeDto") String storeDtoString,
                                              @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
                                              @RequestPart(value = "bannerFile", required = false) MultipartFile bannerFile) {
        StoreDto storeDto = JSON.parseObject(storeDtoString, StoreDto.class);
        if (storeDto.getId() == null) {
            return Result.error("店铺ID不能为空");
        }
        Store oldStore = storeService.getById(storeDto.getId());
        if (oldStore == null) {
            return Result.error("店铺不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!storeService.isStoreOwner(storeDto.getId(), currentUserId)) {
            return Result.error("无权修改此店铺");
        }

        Store store = new Store();
        store.setId(storeDto.getId());
        store.setName(storeDto.getName() != null ? storeDto.getName() : oldStore.getName());
        store.setDescription(storeDto.getDescription() != null ? storeDto.getDescription() : oldStore.getDescription());
        store.setPhone(storeDto.getPhone() != null ? storeDto.getPhone() : oldStore.getPhone());
        store.setAddress(storeDto.getAddress() != null ? storeDto.getAddress() : oldStore.getAddress());
        store.setBusinessLicense(storeDto.getBusinessLicense() != null ? storeDto.getBusinessLicense() : oldStore.getBusinessLicense());
        store.setSort(storeDto.getSort() != null ? storeDto.getSort() : oldStore.getSort());
        store.setSellerId(oldStore.getSellerId());

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String oldLogo = oldStore.getLogo();
        String oldBanner = oldStore.getBanner();
        boolean logoUpdated = false;
        boolean bannerUpdated = false;

        if (logoFile != null && !logoFile.isEmpty()) {
            Map<String, String> logoInfo = uploadLogo(logoFile);
            if (logoInfo != null) {
                store.setLogo(date + "/" + logoInfo.get("fileName"));
                logoUpdated = true;
            }
        } else {
            store.setLogo(oldLogo);
        }
        if (bannerFile != null && !bannerFile.isEmpty()) {
            Map<String, String> bannerInfo = uploadBanner(bannerFile);
            if (bannerInfo != null) {
                store.setBanner(date + "/" + bannerInfo.get("fileName"));
                bannerUpdated = true;
            }
        } else {
            store.setBanner(oldBanner);
        }

        boolean success = storeService.update(store);
        if (success) {
            if (logoUpdated && oldLogo != null) {
                deleteLogoFile(oldLogo);
            }
            if (bannerUpdated && oldBanner != null) {
                deleteBannerFile(oldBanner);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", store.getId());
            data.put("logo", store.getLogo());
            data.put("banner", store.getBanner());
            return Result.success("更新成功", data);
        }
        return Result.error("更新失败");
    }

    /**
     * 获取店铺公开详情（前端展示，含缓存）
     */
    @GetMapping("/detail/{id}")
    public Result<StoreDetailVo> getDetail(@PathVariable Long id) {
        StoreDetailVo storeDetail = storeService.getDetailVO(id);
        if (storeDetail != null) {
            return Result.success(storeDetail);
        }
        return Result.error("店铺不存在");
    }

    /**
     * 管理端查看店铺详情（含完整字段，需 SUPER_ADMIN 权限）
     */
    @GetMapping("/admin/detail/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<StoreAdminVo> getAdminDetail(@PathVariable Long id) {
        Store store = storeService.getById(id);
        if (store != null) {
            return Result.success(StoreAdminVo.fromStore(store));
        }
        return Result.error("店铺不存在");
    }

    /**
     * 获取当前商家自己的店铺信息
     * 从 SecurityContext 获取当前用户 ID，查询其拥有的店铺
     */
    @GetMapping("/my-store")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<StoreSellerVo> getMyStore() {
        Long currentUserId = getCurrentUserId();
        Store store = storeService.getBySellerId(currentUserId);
        if (store != null) {
            return Result.success(StoreSellerVo.fromStore(store));
        }
        return Result.error("您还没有店铺");
    }

    /**
     * 公开分页查询店铺列表（前端展示，按关键字搜索）
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getPage(@RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        List<StoreVo> list = storeService.getPageVO(keyword, page, pageSize);
        int total = storeService.countPageVO(keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 管理端多条件分页查询店铺（支持 ID/关键字/状态/卖家/手机号筛选）
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getAdminPage(@RequestParam(required = false) Long id,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Long sellerId,
                                                    @RequestParam(required = false) String phone,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Store> list = storeService.getAdminPage(id, keyword, status, sellerId, phone, page, pageSize);
        int total = storeService.countAdminPage(id, keyword, status, sellerId, phone);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list.stream().map(StoreAdminVo::fromStore).collect(Collectors.toList()));
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 管理端启用/禁用店铺（status=1 启用, status=0 禁用）
     */
    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam @Min(0) @Max(1) Integer status) {
        boolean success = storeService.updateStatus(id, status);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }

    private Map<String, String> uploadLogo(MultipartFile file) {
        return fileService.uploadFile(file, FileConstants.STORE_LOGO);
    }

    private Map<String, String> uploadBanner(MultipartFile file) {
        return fileService.uploadFile(file, FileConstants.STORE_BANNER);
    }

    private void deleteLogoFile(String logoPath) {
        fileService.deleteFile(logoPath, FileConstants.STORE_LOGO);
    }

    private void deleteBannerFile(String bannerPath) {
        fileService.deleteFile(bannerPath, FileConstants.STORE_BANNER);
    }

    /**
     * 从 SecurityContext 获取当前用户 ID（由 GatewayHeaderAuthenticationFilter 注入）
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                return Long.valueOf(authentication.getPrincipal().toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}