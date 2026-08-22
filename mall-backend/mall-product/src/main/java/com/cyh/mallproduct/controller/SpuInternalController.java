package com.cyh.mallproduct.controller;

import com.cyh.mallcommon.dto.SpuInfoDto;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallproduct.entity.Spu;
import com.cyh.mallproduct.service.SpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * SPU 内部接口控制器（供其他微服务 Feign 调用）
 */
@RestController
@RequestMapping("/internal/spu")
@RequiredArgsConstructor
public class SpuInternalController {

    private final SpuService spuService;

    /**
     * 根据 SPU ID 获取 SPU 基本信息
     */
    @GetMapping("/detail/{id}")
    public Result<SpuInfoDto> getSpuDetail(@PathVariable Long id) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            return Result.error("SPU不存在");
        }
        SpuInfoDto dto = new SpuInfoDto(
                spu.getId(), spu.getName(), spu.getSellerId()
        );
        return Result.success(dto);
    }
}