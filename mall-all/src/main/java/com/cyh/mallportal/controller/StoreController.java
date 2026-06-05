package com.cyh.mallportal.controller;

import cn.hutool.core.io.FileUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallportal.dto.StoreDto;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.FileService;
import com.cyh.mallportal.service.StoreService;
import com.cyh.mallportal.vo.StoreDetailVO;
import com.cyh.mallportal.vo.StoreVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 店铺管理控制器
 */
@RestController
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private FileService fileService;

    /**
     * 新增店铺
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('store:manage') or hasRole('SUPER_ADMIN')")
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

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        store.setSellerId(currentUserId);

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
     */
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('store:manage') or hasRole('SUPER_ADMIN')")
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
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

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
        // 4. 处理文件删除
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
     * 获取店铺详情（公开）
     * 返回 StoreDetailVO，包含店铺名称、Logo、横幅、描述、地址、创建时间，不含内部管理字段
     *
     * @param id 店铺ID
     * @return 店铺详情 VO
     */
    @GetMapping("/detail/{id}")
    public Result<StoreDetailVO> getDetail(@PathVariable Long id) {
        StoreDetailVO storeDetail = storeService.getDetailVO(id);
        if (storeDetail != null) {
            return Result.success(storeDetail);
        }
        return Result.error("店铺不存在");
    }

    /**
     * 获取当前用户的店铺
     */
    @GetMapping("/my-store")
    @PreAuthorize("hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<Store> getMyStore() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        Store store = storeService.getBySellerId(currentUserId);
        if (store != null) {
            return Result.success(store);
        }
        return Result.error("您还没有店铺");
    }

    /**
     * 分页获取公开店铺列表（仅 status=1）
     * 无需登录即可访问，返回 StoreVO（不包含敏感管理字段）
     *
     * @param keyword  搜索关键字（店铺名称模糊匹配，可选）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list, page, pageSize, total }
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getPage(@RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        List<StoreVO> list = storeService.getPageVO(keyword, page, pageSize);
        int total = storeService.countPageVO(keyword);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 管理员分页查询店铺列表（多条件筛选）
     * 可查看全部状态的店铺，返回完整 Store 实体，支持按店铺ID、名称关键字、状态、商家ID、联系电话搜索
     *
     * @param id       店铺ID（精确匹配，可选）
     * @param keyword  店铺名称（模糊匹配，可选）
     * @param status   店铺状态（可选，不传查全部）
     * @param sellerId 商家用户ID（精确匹配，可选）
     * @param phone    联系电话（模糊匹配，可选）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list, page, pageSize, total }
     */
    @GetMapping("/admin/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
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
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 更新店铺状态
     */
    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = storeService.updateStatus(id, status);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }

    /**
     * 上传店铺Logo（上传到uploads/images/stores/logo目录）
     */
    private Map<String, String> uploadLogo(MultipartFile file) {
        return fileService.uploadFile(file, FileConstants.STORE_LOGO);
    }

    /**
     * 上传店铺横幅（上传到uploads/images/stores/banner目录）
     */
    private Map<String, String> uploadBanner(MultipartFile file) {
        return fileService.uploadFile(file, FileConstants.STORE_BANNER);
    }

    private void deleteLogoFile(String logoPath) {
        fileService.deleteFile(logoPath, FileConstants.STORE_LOGO);
    }

    private void deleteBannerFile(String bannerPath) {
        fileService.deleteFile(bannerPath, FileConstants.STORE_BANNER);
    }

    private Map<String, String> uploadImage(MultipartFile file) {
        return fileService.uploadFile(file, FileConstants.STORE_IMAGES);
    }

    private void deleteImageFile(String imagePath) {
        fileService.deleteFile(imagePath, FileConstants.STORE_IMAGES);
    }

    /**
     * 获取当前登录用户的ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        }
        return null;
    }
}