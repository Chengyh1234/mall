package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.CartItemDto;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.CartItemService;
import com.cyh.mallportal.vo.CartItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车管理控制器
 * 提供购物车的增删改查及结算功能
 */
@RestController
@RequestMapping("/cart")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    /**
     * 添加商品到购物车
     * 如果商品已存在，则增加数量
     *
     * @param cartItemDto 购物车项DTO（包含skuId、quantity、notes）
     * @return 添加结果
     */
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> addToCart(@RequestBody CartItemDto cartItemDto) {
        Long userId = getCurrentUserId();

        if (cartItemDto.getSkuId() == null) {
            return Result.error("SKU ID不能为空");
        }
        if (cartItemDto.getQuantity() != null && cartItemDto.getQuantity() <= 0) {
            return Result.error("数量必须大于0");
        }

        boolean success = cartItemService.addToCart(userId, cartItemDto);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("skuId", cartItemDto.getSkuId());
            data.put("quantity", cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1);
            return Result.success("添加成功", data);
        }
        return Result.error("添加失败，商品可能已下架或库存不足");
    }

    /**
     * 更新购物车商品数量
     *
     * @param skuId    SKU ID
     * @param quantity 新的数量
     * @return 更新结果
     */
    @PutMapping("/quantity/{skuId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updateQuantity(@PathVariable Long skuId,
                                       @RequestParam Integer quantity) {
        Long userId = getCurrentUserId();

        boolean success = cartItemService.updateQuantity(userId, skuId, quantity);
        if (success) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败，库存不足或商品不存在");
    }

    /**
     * 设置商品选中状态
     *
     * @param skuId    SKU ID
     * @param selected 是否选中（1-选中 0-未选）
     * @return 设置结果
     */
    @PutMapping("/selected/{skuId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> setSelected(@PathVariable Long skuId,
                                    @RequestParam Integer selected) {
        Long userId = getCurrentUserId();

        if (selected != 0 && selected != 1) {
            return Result.error("选中状态值不正确");
        }

        boolean success = cartItemService.setSelected(userId, skuId, selected);
        if (success) {
            return Result.success("设置成功", null);
        }
        return Result.error("设置失败，商品不存在");
    }

    /**
     * 全选/取消全选
     *
     * @param selected 是否全选（1-全选 0-取消全选）
     * @return 设置结果
     */
    @PutMapping("/selected-all")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> setAllSelected(@RequestParam Integer selected) {
        Long userId = getCurrentUserId();

        if (selected != 0 && selected != 1) {
            return Result.error("选中状态值不正确");
        }

        boolean success = cartItemService.setAllSelected(userId, selected);
        if (success) {
            return Result.success("设置成功", null);
        }
        return Result.error("设置失败");
    }

    /**
     * 从购物车移除商品
     *
     * @param skuId SKU ID
     * @return 移除结果
     */
    @DeleteMapping("/remove/{skuId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> removeFromCart(@PathVariable Long skuId) {
        Long userId = getCurrentUserId();

        boolean success = cartItemService.removeFromCart(userId, skuId);
        if (success) {
            return Result.success("移除成功", null);
        }
        return Result.error("移除失败，商品不存在");
    }

    /**
     * 清空购物车
     *
     * @return 清空结果
     */
    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> clearCart() {
        Long userId = getCurrentUserId();

        boolean success = cartItemService.clearCart(userId);
        if (success) {
            return Result.success("清空成功", null);
        }
        return Result.error("清空失败");
    }

    /**
     * 清空已选中的商品（结算后调用）
     *
     * @return 清空结果
     */
    @DeleteMapping("/clear-selected")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> clearSelected() {
        Long userId = getCurrentUserId();

        boolean success = cartItemService.clearSelected(userId);
        if (success) {
            return Result.success("清空成功", null);
        }
        return Result.error("清空失败");
    }

    /**
     * 获取购物车列表
     *
     * @return 购物车列表（包含商品详情和实时库存）
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CartItemVo>> getCartList() {
        Long userId = getCurrentUserId();

        List<CartItemVo> list = cartItemService.getCartList(userId);
        return Result.success(list);
    }

    /**
     * 获取已选中的购物车商品（用于结算）
     *
     * @return 已选中的购物车商品列表
     */
    @GetMapping("/selected")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CartItemVo>> getSelectedItems() {
        Long userId = getCurrentUserId();

        List<CartItemVo> list = cartItemService.getSelectedItems(userId);
        return Result.success(list);
    }

    /**
     * 获取购物车概要信息
     *
     * @return 购物车概要（商品种数、已选种数、总价）
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getCartSummary() {
        Long userId = getCurrentUserId();

        int totalCount = cartItemService.getCartCount(userId);
        int selectedCount = cartItemService.getSelectedCount(userId);
        BigDecimal totalPrice = cartItemService.getSelectedTotalPrice(userId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", totalCount);
        summary.put("selectedCount", selectedCount);
        summary.put("totalPrice", totalPrice);

        return Result.success(summary);
    }

    /**
     * 获取当前登录用户ID
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
