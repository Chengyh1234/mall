package com.cyh.mallproduct.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallproduct.entity.Sku;
import com.cyh.mallcommon.service.FileService;
import com.cyh.mallproduct.service.SkuService;
import com.cyh.mallproduct.vo.SkuAdminVo;
import com.cyh.mallproduct.vo.SkuStoreVo;
import com.cyh.mallproduct.vo.SkuVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cyh.mallproduct.dto.SkuBatchCreateDto;
import com.cyh.mallproduct.dto.SkuUpdateDto;
import com.cyh.mallproduct.service.SkuAttrService;
import org.springframework.validation.annotation.Validated;

/**
 * SKU管理控制器
 * 提供SKU的增删改查及库存管理功能
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/sku")
public class SkuController {

    private final SkuService skuService;
    private final FileService fileService;
    private final SkuAttrService skuAttrService;

    /**
     * 删除SKU（逻辑删除）
     *
     * @param id SKU ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        // 获取SKU信息（包含图片）
        Sku sku = skuService.getById(id);
        if (sku != null && StringUtils.hasText(sku.getImage())) {
            // 删除图片文件
            deleteImageFile(sku.getImage());
        }

        boolean success = skuService.delete(id);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 删除SPU下所有SKU
     *
     * @param spuId SPU ID
     * @return 删除结果
     */
    @DeleteMapping("/delete-by-spu/{spuId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> deleteBySpuId(@PathVariable Long spuId) {
        // 获取所有SKU并删除图片
        List<Sku> skus = skuService.getBySpuId(spuId);
        for (Sku sku : skus) {
            if (StringUtils.hasText(sku.getImage())) {
                deleteImageFile(sku.getImage());
            }
        }

        boolean success = skuService.deleteBySpuId(spuId);
        if (success) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除SKU
     *
     * @param ids SKU ID列表（JSON数组）
     * @return 删除结果
     */
    @DeleteMapping("/batch-delete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("SKU ID列表不能为空");
        }

        // 删除图片文件
        for (Long id : ids) {
            Sku sku = skuService.getById(id);
            if (sku != null && StringUtils.hasText(sku.getImage())) {
                deleteImageFile(sku.getImage());
            }
        }

        int count = skuService.batchDelete(ids);
        Map<String, Object> data = new HashMap<>();
        data.put("deletedCount", count);
        return Result.success("批量删除成功", data);
    }

    /**
     * 启用SKU
     *
     * @param id SKU ID
     * @return 启用结果
     */
    @PutMapping("/enable/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> enable(@PathVariable Long id) {
        boolean success = skuService.enable(id);
        if (success) {
            return Result.success("启用成功", null);
        }
        return Result.error("启用失败");
    }

    /**
     * 禁用SKU
     *
     * @param id SKU ID
     * @return 禁用结果
     */
    @PutMapping("/disable/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> disable(@PathVariable Long id) {
        boolean success = skuService.disable(id);
        if (success) {
            return Result.success("禁用成功", null);
        }
        return Result.error("禁用失败");
    }

    /**
     * 更新库存
     *
     * @param id    SKU ID
     * @param stock 库存数量
     * @return 更新结果
     */
    @PutMapping("/update-stock")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> updateStock(@RequestParam Long id,
                                    @RequestParam Integer stock) {
        boolean success = skuService.updateStock(id, stock);
        if (success) {
            return Result.success("库存更新成功", null);
        }
        return Result.error("库存更新失败");
    }

    /**
     * 扣减库存
     *
     * @param id       SKU ID
     * @param quantity 扣减数量
     * @return 扣减结果
     */
    @PutMapping("/decrease-stock")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> decreaseStock(@RequestParam Long id,
                                      @RequestParam Integer quantity) {
        boolean success = skuService.decreaseStock(id, quantity);
        if (success) {
            return Result.success("扣减成功", null);
        }
        return Result.error("扣减失败，库存不足");
    }



    /**
     * 获取SPU的库存总量
     *
     * @param spuId SPU ID
     * @return 库存总量
     */
    @GetMapping("/total-stock/{spuId}")
    public Result<Integer> getTotalStock(@PathVariable Long spuId) {
        Integer totalStock = skuService.getTotalStock(spuId);
        return Result.success(totalStock);
    }

    /**
     * 根据SPU ID获取SKU列表（包含销售属性）
     * 公开接口，仅返回启用状态的SKU，对外公开字段
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性）
     */
    @GetMapping("/list-with-attributes")
    public Result<List<SkuVo>> getBySpuIdWithAttributes(@RequestParam Long spuId) {
        List<SkuVo> list = skuService.getBySpuIdWithAttributes(spuId);
        return Result.success(list);
    }

    /**
     * 商家端：根据SPU ID获取SKU列表（包含销售属性）
     * 返回商家经营管理所需的完整字段，不限SKU上下架状态
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性，商家端字段）
     */
    @GetMapping("/store/list-with-attributes")
    @PreAuthorize("hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<List<SkuStoreVo>> getStoreBySpuIdWithAttributes(@RequestParam Long spuId) {
        List<SkuStoreVo> list = skuService.getStoreBySpuIdWithAttributes(spuId);
        return Result.success(list);
    }

    /**
     * 管理员端：根据SPU ID获取SKU列表（包含销售属性）
     * 返回管理员监管所需的全部字段，不限SKU上下架和删除状态
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性，管理员端字段）
     */
    @GetMapping("/admin/list-with-attributes")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<SkuAdminVo>> getAdminBySpuIdWithAttributes(@RequestParam Long spuId) {
        List<SkuAdminVo> list = skuService.getAdminBySpuIdWithAttributes(spuId);
        return Result.success(list);
    }


    // ==================== SKU销售属性管理（从 SkuAttrController 合并） ====================

    /**
     * 创建SKU并绑定销售属性
     * 一步完成单个SKU的新增 + 销售属性绑定
     *
     * @param dto 创建参数（含SKU信息 + 属性值ID列表）
     * @return 创建结果（返回新SKU ID）
     */
    @PostMapping("/attr/create")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> createSkuWithAttrs(@RequestBody @Validated SkuBatchCreateDto dto) {
        Long sellerId = getCurrentUserId();

        Long skuId = skuAttrService.createSkuWithAttrs(dto, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("skuId", skuId);
        return Result.success("创建并绑定成功", data);
    }

    /**
     * 批量创建SKU并绑定销售属性（同SPU）
     * 所有SKU必须在同一个SPU下，且属性值组合不能与已有SKU重复
     *
     * @param dtoList 创建参数列表
     * @return 创建结果（返回每个新SKU ID及其绑定的属性数量）
     */
    @PostMapping("/attr/batch-create")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchCreateSkuWithAttrs(@RequestBody @Validated List<SkuBatchCreateDto> dtoList) {
        Long sellerId = getCurrentUserId();

        Map<Long, Integer> resultMap = skuAttrService.batchCreateSkuWithAttrs(dtoList, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("createdCount", resultMap.size());
        data.put("details", resultMap);
        return Result.success("批量创建并绑定成功", data);
    }

    /**
     * 更新SKU基本信息（不修改销售属性），能更新说明以及存在对应的销售属性
     *
     * @param dto 更新参数
     * @return 更新结果
     */
    @PutMapping("/attr/update-combined")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> updateSkuWithAttrs(@RequestBody @Validated SkuUpdateDto dto) {
        Long sellerId = getCurrentUserId();

        boolean success = skuAttrService.updateSkuWithAttrs(dto, sellerId);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 批量更新SKU基本信息（不修改销售属性）
     *
     * @param dtoList 更新参数列表
     * @return 批量更新结果
     */
    @PutMapping("/attr/batch-update")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchUpdateSkuWithAttrs(@RequestBody @Valid List<SkuUpdateDto> dtoList) {
        Long sellerId = getCurrentUserId();

        int successCount = skuAttrService.batchUpdateSkuWithAttrs(dtoList, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        data.put("totalCount", dtoList != null ? dtoList.size() : 0);
        return Result.success("批量更新完成", data);
    }

    /**
     * 获取当前登录用户ID
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

    private Map<String, String> uploadImage(MultipartFile file) {
        Map<String, String> result = fileService.uploadImage(file, FileConstants.SKU);
        if (result != null) {
            Map<String, String> response = new HashMap<>();
            response.put("relativePath", result.get("relativePath"));
            response.put("imageUrl", "/uploads/images/sku/" + result.get("relativePath"));
            return response;
        }
        return null;
    }

    private void deleteImageFile(String image) {
        fileService.deleteFile(image, FileConstants.SKU);
    }
}