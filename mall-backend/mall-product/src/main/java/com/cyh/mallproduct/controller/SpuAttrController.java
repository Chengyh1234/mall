package com.cyh.mallproduct.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallproduct.dto.SpuAttrFullBindDto;
import com.cyh.mallproduct.dto.SpuBasicAttrBindDto;
import com.cyh.mallproduct.dto.SpuSaleAttrBindDto;
import com.cyh.mallproduct.entity.SpuBasicAttrValue;
import com.cyh.mallproduct.entity.SpuSaleAttrChoice;
import com.cyh.mallproduct.service.SpuAttrService;
import com.cyh.mallproduct.vo.SpuAttrVo;
import com.cyh.mallproduct.vo.SpuAvailableAttrVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商家SPU属性管理控制器 已处理响应
 * 提供商家操作SPU基本属性和销售属性的功能
 * 权限：商家及以上角色（SELLER, ADMIN, SUPER_ADMIN）
 */
@Slf4j
@RestController
@RequestMapping("/spu/attr")
@RequiredArgsConstructor
public class SpuAttrController {

    private final SpuAttrService spuAttrService;

    // ==================== SPU基本属性管理 ====================

    /**
     * 绑定SPU基本属性
     * 商家为SPU绑定基本属性值，支持单选/多选（通过attrValueId）或手动输入（通过manualValue）
     *
     * @param dto 绑定信息（spuId, attrId, attrValueId, manualValue）
     * @return 绑定结果
     */
    @PostMapping("/basic/bind")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> bindBasicAttr(@RequestBody @Validated SpuBasicAttrBindDto dto) {
        Long sellerId = getCurrentUserId();

        Long id = spuAttrService.bindBasicAttr(dto, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("message", "基本属性绑定成功");
        return Result.success(data);
    }

    /**
     * 批量绑定SPU基本属性
     * <p>
     * 遍历传入的绑定列表，逐个绑定SPU基本属性。
     * 某个绑定失败不会影响其他绑定的执行，最终返回成功/总数。
     *
     * @param dtoList 绑定信息列表
     * @return 批量绑定结果（含成功数和总数）
     */
    @PostMapping("/basic/batch-bind")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchBindBasicAttr(@RequestBody List<SpuBasicAttrBindDto> dtoList) {
        Long sellerId = getCurrentUserId();

        int count = spuAttrService.batchBindBasicAttr(dtoList, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", count);
        data.put("totalCount", dtoList != null ? dtoList.size() : 0);
        return Result.success("批量绑定完成", data);
    }

    /**
     * 更新SPU基本属性绑定
     *
     * @param id  记录ID
     * @param dto 新的绑定信息
     * @return 更新结果
     */
    @PutMapping("/basic/update/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> updateBasicAttr(@PathVariable Long id, @RequestBody @Validated SpuBasicAttrBindDto dto) {
        Long sellerId = getCurrentUserId();

        boolean success = spuAttrService.updateBasicAttr(id, dto, sellerId);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 批量更新SPU基本属性绑定
     * <p>
     * 先校验所有更新项，全部校验通过后才执行更新，任一校验失败则全部回滚。
     *
     * @param dtoList 更新信息列表（需包含id、spuId、attrId等字段）
     * @return 批量更新结果
     */
    @PutMapping("/basic/batch-update")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchUpdateBasicAttr(@RequestBody @Valid List<SpuBasicAttrBindDto> dtoList) {
        Long sellerId = getCurrentUserId();

        if (dtoList == null || dtoList.isEmpty()) {
            return Result.error("更新列表不能为空");
        }

        int count = spuAttrService.batchUpdateBasicAttr(dtoList, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", count);
        data.put("totalCount", dtoList.size());
        return Result.success("批量更新完成", data);
    }

    /**
     * 删除SPU基本属性绑定
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/basic/delete/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> deleteBasicAttr(@PathVariable Long id) {
        Long sellerId = getCurrentUserId();

        boolean success = spuAttrService.deleteBasicAttr(id, sellerId);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 获取SPU的基本属性列表 以及替换返回全部的基本属性和销售属性了
     *
     * @param spuId SPU ID
     * @return 基本属性值列表
     */
    // cyhcandel 不再使用
    @GetMapping("/basic/list/{spuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<List<SpuBasicAttrValue>> getBasicAttrsBySpuId(@PathVariable Long spuId) {
        List<SpuBasicAttrValue> list = spuAttrService.getBasicAttrsBySpuId(spuId);
        return Result.success(list);
    }

    // ==================== SPU销售属性管理 ====================

    /**
     * 绑定SPU销售属性
     * 商家为SPU绑定销售属性及可选值（如颜色：[黑,白,红]）
     *
     * @param dto 绑定信息（spuId, attrId, selectedValueIds）
     * @return 绑定结果
     */
    @PostMapping("/sale/bind")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> bindSaleAttr(@RequestBody @Validated SpuSaleAttrBindDto dto) {
        Long sellerId = getCurrentUserId();

        Long id = spuAttrService.bindSaleAttr(dto, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("message", "销售属性绑定成功");
        return Result.success(data);
    }

    /**
     * 批量绑定SPU销售属性
     *
     * @param dtoList 绑定信息列表
     * @return 批量绑定结果
     */
    @PostMapping("/sale/batch-bind")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchBindSaleAttr(@RequestBody @Valid List<SpuSaleAttrBindDto> dtoList) {
        Long sellerId = getCurrentUserId();

        int count = spuAttrService.batchBindSaleAttr(dtoList, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", count);
        data.put("totalCount", dtoList != null ? dtoList.size() : 0);
        return Result.success("批量绑定完成", data);
    }

    /**
     * 更新SPU销售属性绑定
     * 如果移除了某个已被SKU绑定的属性值，将会更新失败并提示
     *
     * @param id  记录ID
     * @param dto 新的绑定信息
     * @return 更新结果
     */
    @PutMapping("/sale/update/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> updateSaleAttr(@PathVariable Long id, @RequestBody @Validated SpuSaleAttrBindDto dto) {
        Long sellerId = getCurrentUserId();

        boolean success = spuAttrService.updateSaleAttr(id, dto, sellerId);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    /**
     * 批量更新SPU销售属性绑定
     * 先校验所有变更，如果任何一项存在"被移除的属性值已被SKU绑定"的情况，
     * 则全部失败回滚，并提示前端先处理相关SKU
     *
     * @param dtoList 更新信息列表（每个元素需包含id、spuId、attrId、selectedValueIds）
     * @return 批量更新结果
     */
    @PutMapping("/sale/batch-update")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> batchUpdateSaleAttr(@RequestBody List<SpuSaleAttrBindDto> dtoList) {
        Long sellerId = getCurrentUserId();

        if (dtoList == null || dtoList.isEmpty()) {
            return Result.error("更新列表不能为空");
        }

        int count = spuAttrService.batchUpdateSaleAttr(dtoList, sellerId);
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", count);
        data.put("totalCount", dtoList.size());
        return Result.success("批量更新完成", data);
    }

    /**
     * 删除SPU销售属性绑定
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/sale/delete/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<Void> deleteSaleAttr(@PathVariable Long id) {
        Long sellerId = getCurrentUserId();

        boolean success = spuAttrService.deleteSaleAttr(id, sellerId);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    /**
     * 获取SPU的销售属性列表
     * 返回销售属性及其可选值列表
     *
     * @param spuId SPU ID
     * @return 销售属性详情列表（包含属性值）
     */
    // cyhcandel 已经不使用了
    @GetMapping("/sale/list/{spuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<List<SpuAttrVo.SpuSaleAttrDetailVo>> getSaleAttrsBySpuId(@PathVariable Long spuId) {
        List<SpuAttrVo.SpuSaleAttrDetailVo> list = spuAttrService.getSaleAttrsWithValuesBySpuId(spuId);
        return Result.success(list);
    }

    // ==================== 综合查询 ====================

    /**
     * 获取SPU的所有属性（基本属性+销售属性）
     * 包含完整的属性名称、属性值等信息
     *
     * @param spuId SPU ID
     * @return SPU属性完整信息
     */
    @GetMapping("/all/{spuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<SpuAttrVo> getAllAttrsBySpuId(@PathVariable Long spuId) {
        SpuAttrVo vo = spuAttrService.getAllAttrsBySpuId(spuId);
        return Result.success(vo);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
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

    // cyhcandel
    /**
     * 获取SPU可绑定的属性列表（没有进行使用）
     * 根据SPU的分类，显示分类下的所有属性，并标记哪些已绑定
     *
     * @param spuId SPU ID
     * @return 可用属性列表
     */
    @GetMapping("/available/{spuId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    public Result<SpuAvailableAttrVo> getAvailableAttrs(@PathVariable Long spuId) {
        SpuAvailableAttrVo vo = spuAttrService.getAvailableAttrsBySpuId(spuId);
        return Result.success(vo);
    }

    /**
     * 一次性为SPU绑定所有属性（基本属性+销售属性）
     * 会先清除该SPU原有的绑定，再批量绑定新的
     *
     * @param dto 完整属性绑定信息
     * @return 绑定结果
     */
    // cyhcandel
    //@PostMapping("/bind-all")
    //@PreAuthorize("hasRole('SELLER') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')")
    //public Result<Map<String, Object>> bindAllAttrs(@RequestBody @Validated SpuAttrFullBindDto dto) {
    //    Long sellerId = getCurrentUserId();
    //    if (sellerId == null) {
    //        return Result.error("用户未登录");
    //    }
    //
    //    Map<String, Object> result = spuAttrService.bindAllAttrs(dto, sellerId);
    //    return Result.success("属性绑定完成", result);
    //}
}