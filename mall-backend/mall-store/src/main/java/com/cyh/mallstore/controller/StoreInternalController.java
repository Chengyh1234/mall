package com.cyh.mallstore.controller;

import com.cyh.mallcommon.dto.StoreInfoVo;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.service.StoreService;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 店铺内部接口控制器（供其他微服务 Feign 调用）
 * <p>
 * 路径统一使用 /internal/store/ 前缀，区别于对外公开接口。
 * 返回纯数据 DTO，不含脱敏、缓存等对外逻辑。
 */
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