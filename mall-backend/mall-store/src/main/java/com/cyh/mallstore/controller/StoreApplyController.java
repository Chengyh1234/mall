package com.cyh.mallstore.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.service.StoreService;
import com.cyh.mallstore.vo.StoreApplyAdminVo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 开店申请流程控制器
 * 提供用户端开店申请、重新提交、重新开店、注销店铺
 * 以及管理端审核通过/驳回等全流程接口
 */
@RestController
@RequiredArgsConstructor
public class StoreApplyController {

    private final StoreService storeService;

    // ==================== 用户端接口 ====================

    /**
     * 用户提交开店申请
     * 校验：同一用户不能同时拥有多个正常店铺；已存在申请时不可重复提交
     * 店铺名称不能重复，申请后状态为"审核中（status=2）"
     */
    @PostMapping("/store/apply")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> apply(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String address) {
        Long userId = getCurrentUserId();
        storeService.apply(userId, name, description, phone, address);
        return Result.success("开店申请已提交，请等待管理员审核", null);
    }

    /**
     * 查询当前用户的开店申请状态
     * 返回店铺基本信息及审核状态、驳回原因
     */
    @GetMapping("/store/apply/status")
    @PreAuthorize("hasRole('USER')")
    public Result<Map<String, Object>> getMyApply() {
        Long userId = getCurrentUserId();
        Store store = storeService.getMyApply(userId);
        if (store == null) {
            return Result.error("您还没有提交过开店申请");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", store.getId());
        data.put("name", store.getName());
        data.put("description", store.getDescription());
        data.put("phone", store.getPhone());
        data.put("address", store.getAddress());
        data.put("status", store.getStatus());
        data.put("rejectReason", store.getRejectReason());
        return Result.success(data);
    }

    /**
     * 审核失败后重新提交开店申请
     * 仅 status=3（审核失败）的店铺可重新提交，提交后状态变为"审核中"
     * 支持修改店铺名称（需校验唯一性）
     */
    @PostMapping("/store/apply/re-submit")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> reSubmit(@RequestParam Long storeId,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String address) {
        Long userId = getCurrentUserId();
        if (!storeService.isStoreOwner(storeId, userId)) {
            return Result.error("无权操作该店铺");
        }
        storeService.reSubmit(storeId, name, description, phone, address);
        return Result.success("开店申请已重新提交，请等待管理员审核", null);
    }

    /**
     * 已注销店铺重新申请开店
     * 仅 status=0（已注销）的店铺可重新申请，提交后状态变为"审核中"
     */
    @PostMapping("/store/apply/reopen")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> reApply(@RequestParam Long storeId,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address) {
        Long userId = getCurrentUserId();
        storeService.reApply(storeId, userId, name, description, phone, address);
        return Result.success("开店申请已重新提交，请等待管理员审核", null);
    }

    // ==================== 管理端接口 ====================

    /**
     * 管理员查看待审核/已驳回的开店申请列表（分页）
     * @param status 状态值：2=审核中, 3=审核失败
     */
    @GetMapping("/admin/store/apply/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Map<String, Object>> getPendingPage(
            @RequestParam @Min(value = 2, message = "状态值只能为2或3")
            @Max(value = 3, message = "状态值只能为2或3") Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Store> list = storeService.getPendingPage(status, page, pageSize);
        int total = storeService.countPending(status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list.stream().map(StoreApplyAdminVo::fromStore).collect(Collectors.toList()));
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 管理员审核通过开店申请
     * 将店铺状态从"审核中（status=2）"改为"正常（status=1）"，清除驳回原因
     */
    @PutMapping("/admin/store/apply/approve/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> approve(@PathVariable Long id) {
        storeService.approve(id);
        return Result.success("审核通过，店铺已启用", null);
    }

    /**
     * 管理员驳回开店申请
     * 将店铺状态从"审核中（status=2）"改为"审核失败（status=3）"，记录驳回原因
     */
    @PutMapping("/admin/store/apply/reject/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String rejectReason) {
        storeService.reject(id, rejectReason);
        return Result.success("已驳回开店申请", null);
    }

    /**
     * 用户主动注销店铺
     * 仅店铺所有者可操作，将店铺状态改为"已注销（status=0）"
     */
    @PostMapping("/store/deactivate")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deactivate(@RequestParam Long storeId) {
        Long userId = getCurrentUserId();
        if (!storeService.isStoreOwner(storeId, userId)) {
            return Result.error("无权操作该店铺");
        }
        storeService.deactivate(storeId, userId);
        return Result.success("店铺已注销", null);
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