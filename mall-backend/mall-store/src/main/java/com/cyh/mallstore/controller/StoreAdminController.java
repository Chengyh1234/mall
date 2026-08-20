package com.cyh.mallstore.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import com.cyh.mallstore.dto.StoreAdminDto;
import com.cyh.mallstore.entity.StoreAdmin;
import com.cyh.mallstore.service.StoreAdminService;
import com.cyh.mallstore.service.StoreService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺管理员管理控制器
 * 提供店铺管理员的增删改查、权限分配、状态管理等功能
 * 操作权限校验：仅店主（SUPER_ADMIN）可管理本店管理员
 */
@RestController
@RequestMapping("/store-admin")
@RequiredArgsConstructor
public class StoreAdminController {

    private final StoreAdminService storeAdminService;
    private final StoreService storeService;

    /**
     * 添加店铺管理员
     * 校验当前用户是否为该店铺的店主（SUPER_ADMIN），仅店主可管理本店管理员
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> add(@RequestBody @Validated(Create.class) StoreAdminDto storeAdminDto) {
        Long currentUserId = getCurrentUserId();
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
     * 更新管理员角色或状态
     * 仅店主可操作，非空字段覆盖更新
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> update(@RequestBody @Validated(Update.class) StoreAdminDto storeAdminDto) {
        StoreAdmin oldAdmin = storeAdminService.getById(storeAdminDto.getId());
        if (oldAdmin == null) {
            return Result.error("管理员不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!storeService.isStoreOwner(oldAdmin.getStoreId(), currentUserId)) {
            return Result.error("无权操作此店铺");
        }
        StoreAdmin storeAdmin = new StoreAdmin();
        storeAdmin.setId(storeAdminDto.getId());
        storeAdmin.setRole(storeAdminDto.getRole() != null ? storeAdminDto.getRole() : oldAdmin.getRole());
        storeAdmin.setStatus(storeAdminDto.getStatus() != null ? storeAdminDto.getStatus() : oldAdmin.getStatus());
        boolean success = storeAdminService.update(storeAdmin);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }

    /**
     * 删除店铺管理员
     * 仅店主可操作，删除前校验管理员是否存在
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        StoreAdmin admin = storeAdminService.getById(id);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        Long currentUserId = getCurrentUserId();
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
     * 查询管理员详情
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
     * 查询指定店铺的所有管理员列表
     */
    @GetMapping("/list/{storeId}")
    public Result<List<StoreAdmin>> getByStoreId(@PathVariable Long storeId) {
        List<StoreAdmin> list = storeAdminService.getByStoreId(storeId);
        return Result.success(list);
    }

    /**
     * 查询当前用户的管理员信息（适用于店主或管理员查看自己的 admin 记录）
     */
    @GetMapping("/my-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<StoreAdmin> getMyAdmin() {
        Long currentUserId = getCurrentUserId();
        StoreAdmin storeAdmin = storeAdminService.getByUserId(currentUserId);
        if (storeAdmin != null) {
            return Result.success(storeAdmin);
        }
        return Result.error("您不是店铺管理员");
    }

    /**
     * 启用/禁用店铺管理员（status=1 启用, status=0 禁用）
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
        if (!storeService.isStoreOwner(admin.getStoreId(), currentUserId)) {
            return Result.error("无权操作此店铺");
        }
        boolean success = storeAdminService.updateStatus(id, status);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败");
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getCredentials() != null) {
            try {
                return Long.valueOf(authentication.getCredentials().toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}