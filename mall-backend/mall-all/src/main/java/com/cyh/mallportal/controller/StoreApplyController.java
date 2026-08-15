package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.StoreService;
import com.cyh.mallportal.vo.StoreApplyAdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 开店申请控制器    已修改响应
 * 提供用户开店申请 和 管理员审核的接口
 */
@RestController
public class StoreApplyController {

    @Autowired
    private StoreService storeService;

    // ==================== 用户端接口 ====================

    /**
     * 用户提交开店申请
     * 校验用户无SELLER角色、无审核中的申请后，创建status=2(审核中)的店铺
     *
     * @param name        店铺名称
     * @param description 店铺描述（可选）
     * @param phone       联系电话（可选）
     * @param address     店铺地址（可选）
     * @return 创建的店铺信息
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
     * 用户查看自己的开店申请状态
     * 返回店铺 ID、名称、描述、电话、地址、审核状态和驳回原因
     * status=0（已注销）时前端可用返回数据填充重新申请表单
     *
     * @return 店铺信息（含完整店铺字段）
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
     * 用户重新提交开店申请（被驳回后修改重新提交）
     *
     * @param storeId     店铺ID
     * @param name        店铺名称
     * @param description 店铺描述（可选）
     * @param phone       联系电话（可选）
     * @param address     店铺地址（可选）
     * @return 操作结果
     */
    @PostMapping("/store/apply/re-submit")
    @PreAuthorize("hasRole('USER')")
    public Result<Void> reSubmit(@RequestParam Long storeId,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) String phone,
                                 @RequestParam(required = false) String address) {
        Long userId = getCurrentUserId();
        // 校验仅店铺所有者可操作
        if (!storeService.isStoreOwner(storeId, userId)) {
            return Result.error("无权操作该店铺");
        }
        storeService.reSubmit(storeId, name, description, phone, address);
        return Result.success("开店申请已重新提交，请等待管理员审核", null);
    }

    /**
     * 已注销店铺重新申请开店
     * 校验用户有status=0(已注销)的店铺，更新信息并重置status=2(审核中)
     *
     * @param storeId     店铺ID
     * @param name        店铺名称（可选，不传则沿用原名）
     * @param description 店铺描述（可选）
     * @param phone       联系电话（可选）
     * @param address     店铺地址（可选）
     * @return 操作结果
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
     * 管理员分页查询指定审核状态的开店申请
     * 仅支持 status=2（审核中）或 status=3（审核失败）
     *
     * @param status   申请状态（2=审核中，3=审核失败）
     * @param page     页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 分页数据：{ list, page, pageSize, total }
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
        data.put("list", list.stream()
                .map(StoreApplyAdminVo::fromStore)
                .collect(Collectors.toList()));
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 管理员审核通过开店申请
     * 店铺status改为1(正常)，用户自动获得SELLER角色
     *
     * @param id 店铺ID
     * @return 操作结果
     */
    @PutMapping("/admin/store/apply/approve/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> approve(@PathVariable Long id) {
        storeService.approve(id);
        return Result.success("审核通过，店铺已启用", null);
    }

    /**
     * 管理员驳回开店申请
     * 店铺status改为3(审核失败)，记录驳回原因
     *
     * @param id           店铺ID
     * @param rejectReason 驳回原因
     * @return 操作结果
     */
    @PutMapping("/admin/store/apply/reject/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestParam String rejectReason) {
        storeService.reject(id, rejectReason);
        return Result.success("已驳回开店申请", null);
    }

    /**
     * 商家注销店铺，更新对应的redis缓存
     * 校验店铺下所有订单均为完结状态（已完成/已取消/已退款）后方可注销，
     * 注销后店铺 status 置为 0（禁用/已注销）
     *
     * @param storeId 店铺ID
     * @return 操作结果
     */
    @PostMapping("/store/deactivate")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deactivate(@RequestParam Long storeId) {
        Long userId = getCurrentUserId();
        // 校验店铺所有权
        if (!storeService.isStoreOwner(storeId, userId)) {
            return Result.error("无权操作该店铺");
        }
        storeService.deactivate(storeId, userId);
        return Result.success("店铺已注销", null);
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