package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import com.cyh.mallportal.dto.StoreAdminDto;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.StoreAdmin;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.StoreAdminService;
import com.cyh.mallportal.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺管理员控制器-----------------暂时没有这个功能
 */
@RestController
@RequestMapping("/store-admin")
public class StoreAdminController {

    @Autowired
    private StoreAdminService storeAdminService;

    @Autowired
    private StoreService storeService;

    /**
     * 新增店铺管理员
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> add(@RequestBody @Validated(Create.class) StoreAdminDto storeAdminDto) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        if (!storeService.isStoreOwner(storeAdminDto.getStoreId(), currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        StoreAdmin storeAdmin = new StoreAdmin();
        storeAdmin.setStoreId(storeAdminDto.getStoreId());
        storeAdmin.setUserId(storeAdminDto.getUserId());
        storeAdmin.setRole(storeAdminDto.getRole());
        storeAdmin.setStatus(storeAdminDto.getStatus());

        Long id = storeAdminService.add(storeAdmin);
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            return Result.success("添加成功", data);
        }
        return Result.error("添加失败");
    }

    /**
     * 更新店铺管理员
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@RequestBody @Validated(Update.class) StoreAdminDto storeAdminDto) {
        StoreAdmin oldAdmin = storeAdminService.getById(storeAdminDto.getId());
        if (oldAdmin == null) {
            return Result.error("管理员不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        if (!storeService.isStoreOwner(oldAdmin.getStoreId(), currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        StoreAdmin storeAdmin = new StoreAdmin();
        storeAdmin.setId(storeAdminDto.getId());
        if (storeAdminDto.getRole() != null) {
            storeAdmin.setRole(storeAdminDto.getRole());
        } else {
            storeAdmin.setRole(oldAdmin.getRole());
        }
        if (storeAdminDto.getStatus() != null) {
            storeAdmin.setStatus(storeAdminDto.getStatus());
        } else {
            storeAdmin.setStatus(oldAdmin.getStatus());
        }

        boolean success = storeAdminService.update(storeAdmin);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }

    /**
     * 删除店铺管理员
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        StoreAdmin admin = storeAdminService.getById(id);
        if (admin == null) {
            return Result.error("管理员不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        if (!storeService.isStoreOwner(admin.getStoreId(), currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        boolean success = storeAdminService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 获取管理员详情
     */
    @GetMapping("/detail/{id}")
    public Result<StoreAdmin> getById(@PathVariable Long id) {
        StoreAdmin storeAdmin = storeAdminService.getById(id);
        if (storeAdmin != null) {
            return Result.success(storeAdmin);
        }
        return Result.error("管理员不存在");
    }

    /**
     * 获取店铺的管理员列表
     */
    @GetMapping("/list/{storeId}")
    public Result<List<StoreAdmin>> getByStoreId(@PathVariable Long storeId) {
        List<StoreAdmin> list = storeAdminService.getByStoreId(storeId);
        return Result.success(list);
    }

    /**
     * 获取当前用户的管理员信息
     */
    @GetMapping("/my-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<StoreAdmin> getMyAdmin() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        StoreAdmin storeAdmin = storeAdminService.getByUserId(currentUserId);
        if (storeAdmin != null) {
            return Result.success(storeAdmin);
        }
        return Result.error("您不是店铺管理员");
    }

    /**
     * 更新管理员状态
     */
    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam @Min(value = 0, message = "状态值不能小于0")
                                     @Max(value = 1, message = "状态值不能大于1") Integer status) {
        StoreAdmin admin = storeAdminService.getById(id);
        if (admin == null) {
            return Result.error("管理员不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        if (!storeService.isStoreOwner(admin.getStoreId(), currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        boolean success = storeAdminService.updateStatus(id, status);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
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