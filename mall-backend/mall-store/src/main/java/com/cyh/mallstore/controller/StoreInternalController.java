package com.cyh.mallstore.controller;

import com.cyh.mallcommon.dto.StoreInfoVo;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺内部接口控制器（供其他微服务 Feign 调用）
 * <p>
 * 路径统一使用 /internal/store/ 前缀，区别于对外公开接口。
 * 返回纯数据 DTO，不含脱敏、缓存等对外逻辑。
 */
@Slf4j
@RestController
@RequestMapping("/internal/store")
@RequiredArgsConstructor
public class StoreInternalController {

    private final StoreService storeService;

    /**
     * 根据当前商家获取店铺基本信息
     * 供 mall-product 通过 Feign 调用，替代直连数据库
     */
    @GetMapping("/by-seller")
    public Result<StoreInfoVo> getStoreBySeller() {
        Long currentUserId = getCurrentUserId();
        Store store = storeService.getBySellerId(currentUserId);
        if (store != null) {
            StoreInfoVo vo = new StoreInfoVo(store.getId(), store.getName(), store.getLogo(),
                    store.getBanner(), store.getDescription(), store.getAddress(), store.getCreatedAt());
            return Result.success(vo);
        }
        return Result.error("您还没有店铺");
    }

    /**
     * 根据卖家 ID 查询店铺
     * 供 mall-user 通过 Feign 调用（升级商家时检查是否已有店铺）
     */
    @GetMapping("/by-seller-id/{sellerId}")
    public Result<StoreInfoVo> getBySellerId(@PathVariable Long sellerId) {
        Store store = storeService.getBySellerId(sellerId);
        if (store != null) {
            StoreInfoVo vo = new StoreInfoVo(store.getId(), store.getName(), store.getLogo(),
                    store.getBanner(), store.getDescription(), store.getAddress(), store.getCreatedAt());
            return Result.success(vo);
        }
        return Result.error("店铺不存在");
    }

    /**
     * 根据店铺 ID 获取店铺基本信息
     * 供 mall-product 通过 Feign 调用，替代直连数据库
     */
    @GetMapping("/detail/{id}")
    public Result<StoreInfoVo> getStoreDetail(@PathVariable Long id) {
        Store store = storeService.getById(id);
        if (store != null) {
            StoreInfoVo vo = new StoreInfoVo(store.getId(), store.getName(), store.getLogo(),
                    store.getBanner(), store.getDescription(), store.getAddress(), store.getCreatedAt());
            return Result.success(vo);
        }
        return Result.error("店铺不存在");
    }

    /**
     * 创建默认店铺（超级管理员升级商家时自动调用）
     * <p>
     * 供 mall-user 通过 Feign 调用，用于创建默认店铺。
     * 只需要店铺名称和卖家 ID，店铺状态默认启用。
     */
    @PostMapping("/create-default")
    public Result<Long> createDefaultStore(@RequestBody CreateDefaultStoreRequest request) {
        log.info("内部接口 - 创建默认店铺: sellerId={}, storeName={}", request.getSellerId(), request.getStoreName());

        if (request.getSellerId() == null) {
            return Result.error("卖家 ID 不能为空");
        }
        if (request.getStoreName() == null || request.getStoreName().isBlank()) {
            return Result.error("店铺名称不能为空");
        }

        // 检查是否已有店铺
        Store existing = storeService.getBySellerId(request.getSellerId());
        if (existing != null) {
            log.warn("卖家已有店铺，跳过创建: sellerId={}, storeId={}", request.getSellerId(), existing.getId());
            return Result.success(existing.getId());
        }

        Store store = new Store();
        store.setName(request.getStoreName());
        store.setSellerId(request.getSellerId());
        store.setStatus(1);
        Long storeId = storeService.add(store);
        return Result.success(storeId);
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

    /**
     * 创建默认店铺请求 DTO
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateDefaultStoreRequest {
        private Long sellerId;
        private String storeName;
    }
}