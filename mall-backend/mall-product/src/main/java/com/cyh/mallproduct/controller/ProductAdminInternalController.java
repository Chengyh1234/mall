package com.cyh.mallproduct.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallproduct.entity.Spu;
import com.cyh.mallproduct.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理内部接口控制器（供管理员仪表盘 Feign 调用）
 */
@RestController
@RequestMapping("/internal/product/admin")
@RequiredArgsConstructor
public class ProductAdminInternalController {

    private final SpuMapper spuMapper;

    /**
     * 获取平台商品总数（未删除的 SPU）
     */
    @GetMapping("/product-count")
    public Result<Long> getProductCount() {
        return Result.success(spuMapper.selectCount(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getIsDeleted, false)));
    }
}