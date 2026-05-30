package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.SkuBatchCreateDto;
import com.cyh.mallportal.dto.SkuSaleAttrBindDto;
import com.cyh.mallportal.dto.SkuUpdateDto;
import com.cyh.mallportal.entity.SkuSaleAttrValue;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.SkuAttrService;
import com.cyh.mallportal.vo.SkuAttrVo;
import com.cyh.mallportal.vo.SkuAvailableAttrVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家SKU属性管理控制器
 * 提供商家操作SKU销售属性的功能
 * 权限：商家及以上角色（SELLER, ADMIN, SUPER_ADMIN）
 */
@Slf4j
@RestController
@RequestMapping("/sku/attr")
@RequiredArgsConstructor
public class SkuAttrController {

    private final SkuAttrService skuAttrService;

    // ==================== SKU销售属性管理 ====================

    /**
     * 绑定SKU销售属性   不需要===========================
     * 商家为SKU绑定销售属性值（如颜色、内存、存储等）
     * 注意：此操作会覆盖该SKU原有的销售属性绑定
     *
     * @param dto 绑定信息（skuId, attrValueIds）
     * @return 绑定结果
     */
    @PostMapping("/bind")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> bindSaleAttr(@RequestBody @Validated SkuSaleAttrBindDto dto) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            int count = skuAttrService.bindSaleAttr(dto, sellerId);
            Map<String, Object> data = new HashMap<>();
            data.put("bindCount", count);
            data.put("message", "销售属性绑定成功");
            return Result.success(data);
        } catch (Exception e) {
            log.error("绑定SKU销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量绑定SKU销售属性，不需要==============================
     *
     * @param dtoList 绑定信息列表
     * @return 批量绑定结果
     */
    @PostMapping("/batch-bind")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchBindSaleAttr(@RequestBody List<SkuSaleAttrBindDto> dtoList) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            int count = skuAttrService.batchBindSaleAttr(dtoList, sellerId);
            Map<String, Object> data = new HashMap<>();
            data.put("totalBindCount", count);
            data.put("skuCount", dtoList != null ? dtoList.size() : 0);
            return Result.success("批量绑定完成", data);
        } catch (Exception e) {
            log.error("批量绑定SKU销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建SKU并绑定销售属性
     * 一步完成单个SKU的新增 + 销售属性绑定
     *
     * @param dto 创建参数（含SKU信息 + 属性值ID列表）
     * @return 创建结果（返回新SKU ID）
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> createSkuWithAttrs(@RequestBody @Validated SkuBatchCreateDto dto) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            Long skuId = skuAttrService.createSkuWithAttrs(dto, sellerId);
            Map<String, Object> data = new HashMap<>();
            data.put("skuId", skuId);
            return Result.success("创建并绑定成功", data);
        } catch (Exception e) {
            log.error("创建SKU并绑定销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量创建SKU并绑定销售属性（同SPU）
     * 所有SKU必须在同一个SPU下，且属性值组合不能与已有SKU重复
     *
     * @param dtoList 创建参数列表
     * @return 创建结果（返回每个新SKU ID及其绑定的属性数量）
     */
    @PostMapping("/batch-create")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchCreateSkuWithAttrs(@RequestBody @Validated List<SkuBatchCreateDto> dtoList) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            Map<Long, Integer> resultMap = skuAttrService.batchCreateSkuWithAttrs(dtoList, sellerId);
            Map<String, Object> data = new HashMap<>();
            data.put("createdCount", resultMap.size());
            data.put("details", resultMap);
            return Result.success("批量创建并绑定成功", data);
        } catch (Exception e) {
            log.error("批量创建SKU并绑定销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新SKU基本信息（不修改销售属性），能更新说明以及存在对应的销售属性
     *
     * @param dto 更新参数
     * @return 更新结果
     */
    @PutMapping("/update-combined")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> updateSkuWithAttrs(@RequestBody @Validated SkuUpdateDto dto) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            boolean success = skuAttrService.updateSkuWithAttrs(dto, sellerId);
            return success ? Result.success("更新成功", null) : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新SKU信息及销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量更新SKU基本信息（不修改销售属性）
     *
     * @param dtoList 更新参数列表
     * @return 批量更新结果
     */
    @PutMapping("/batch-update")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchUpdateSkuWithAttrs(@RequestBody List<SkuUpdateDto> dtoList) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            int successCount = skuAttrService.batchUpdateSkuWithAttrs(dtoList, sellerId);
            Map<String, Object> data = new HashMap<>();
            data.put("successCount", successCount);
            data.put("totalCount", dtoList != null ? dtoList.size() : 0);
            return Result.success("批量更新完成", data);
        } catch (Exception e) {
            log.error("批量更新SKU信息及销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新SKU销售属性绑定 不需要====================
     * 先删除原有绑定，再添加新的绑定
     *
     * @param skuId        SKU ID
     * @param attrValueIds 新的属性值ID列表
     * @return 更新结果
     */
    @PutMapping("/update/{skuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> updateSaleAttr(
            @PathVariable Long skuId,
            @RequestBody List<Long> attrValueIds) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            boolean success = skuAttrService.updateSaleAttr(skuId, attrValueIds, sellerId);
            return success ? Result.success("更新成功", null) : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新SKU销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除SKU销售属性绑定,这个不需要========================我需要的sku都要先绑定销售属性
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> deleteSaleAttr(@PathVariable Long id) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            boolean success = skuAttrService.deleteSaleAttr(id, sellerId);
            return success ? Result.success("删除成功", null) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除SKU销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除SKU的所有销售属性绑定,这个不需要========================我需要的sku都要先绑定销售属性
     *
     * @param skuId SKU ID
     * @return 删除结果
     */
    @DeleteMapping("/delete-all/{skuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> deleteAllSaleAttrs(@PathVariable Long skuId) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }

        try {
            int count = skuAttrService.deleteAllSaleAttrsBySkuId(skuId, sellerId);
            Map<String, Object> data = new HashMap<>();
            data.put("deletedCount", count);
            return Result.success("删除成功", data);
        } catch (Exception e) {
            log.error("删除SKU所有销售属性失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // ==================== 查询接口 ====================

    /**
     * 获取SKU的销售属性列表
     *
     * @param skuId SKU ID
     * @return 销售属性值列表
     */
    @GetMapping("/list/{skuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<List<SkuSaleAttrValue>> getSaleAttrsBySkuId(@PathVariable Long skuId) {
        try {
            List<SkuSaleAttrValue> list = skuAttrService.getSaleAttrsBySkuId(skuId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取SKU销售属性列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取SKU的销售属性详情
     * 包含完整的属性名称、属性值等信息
     *
     * @param skuId SKU ID
     * @return SKU销售属性详情
     */
    @GetMapping("/detail/{skuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<SkuAttrVo> getSkuAttrDetail(@PathVariable Long skuId) {
        try {
            SkuAttrVo vo = skuAttrService.getSkuAttrDetail(skuId);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取SKU销售属性详情失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // ==================== 校验接口 ====================

    /**
     * 校验SKU属性组合是否合法
     * 检查SKU绑定的属性值是否都属于该SPU选择的销售属性
     *
     * @param spuId        SPU ID
     * @param attrValueIds 属性值ID列表
     * @return 校验结果
     */
    @PostMapping("/validate/{spuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> validateAttrCombination(
            @PathVariable Long spuId,
            @RequestBody List<Long> attrValueIds) {
        try {
            boolean valid = skuAttrService.validateAttrCombination(spuId, attrValueIds);
            Map<String, Object> data = new HashMap<>();
            data.put("valid", valid);
            data.put("message", valid ? "属性组合合法" : "属性组合不合法，请检查属性值是否属于该SPU的销售属性");
            return Result.success(data);
        } catch (Exception e) {
            log.error("校验SKU属性组合失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return user.getId();
        }
        return null;
    }

    /**
     * 获取SKU可选择的销售属性值列表
     * 根据SPU ID，获取该SPU选择的销售属性及可选属性值，供SKU创建时选择
     *
     * @param spuId SPU ID
     * @return 可用属性列表
     */
    @GetMapping("/available/{spuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<SkuAvailableAttrVo> getAvailableAttrs(@PathVariable Long spuId) {
        try {
            SkuAvailableAttrVo vo = skuAttrService.getAvailableAttrsBySpuId(spuId);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取SKU可选择属性列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
