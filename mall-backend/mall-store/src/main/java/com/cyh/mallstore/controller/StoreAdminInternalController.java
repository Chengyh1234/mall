package com.cyh.mallstore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺管理内部接口控制器（供管理员仪表盘 Feign 调用）
 */
@RestController
@RequestMapping("/internal/store/admin")
@RequiredArgsConstructor
public class StoreAdminInternalController {

    private final StoreMapper storeMapper;

    /**
     * 获取已入驻卖家数（状态为正常的店铺数）
     */
    @GetMapping("/settled-seller-count")
    public Result<Long> getSettledSellerCount() {
        return Result.success(storeMapper.selectCount(new LambdaQueryWrapper<Store>()
                .eq(Store::getStatus, 1)));
    }
}