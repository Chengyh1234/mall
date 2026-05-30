package com.cyh.mallportal.controller;

import com.alibaba.fastjson2.JSON;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.dto.SkuDto;
import com.cyh.mallportal.entity.Sku;
import com.cyh.mallportal.service.FileService;
import com.cyh.mallportal.service.SkuService;
import com.cyh.mallportal.vo.SkuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SKU管理控制器
 * 提供SKU的增删改查及库存管理功能
 */
@RestController
@RequestMapping("/sku")
public class SkuController {

    @Autowired
    private SkuService skuService;

    @Autowired
    private FileService fileService;

    /**
     * 新增SKU（支持图片上传）
     *
     * @param skuDtoString SKU信息的JSON字符串
     * @param imageFile    上传的SKU图片（非必填）
     * @return 新增结果
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> add(@RequestPart(value = "skuDto") String skuDtoString,
                                           @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        // 解析SKU DTO
        SkuDto skuDto = JSON.parseObject(skuDtoString, SkuDto.class);

        // 参数校验
        if (skuDto.getSpuId() == null) {
            return Result.error("SPU ID不能为空");
        }
        if (skuDto.getPrice() == null || skuDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("价格不能为空且必须大于0");
        }

        // 创建SKU实体
        Sku sku = new Sku();
        sku.setSpuId(skuDto.getSpuId());
        sku.setPrice(skuDto.getPrice());
        sku.setMarketPrice(skuDto.getMarketPrice());
        sku.setCostPrice(skuDto.getCostPrice());
        sku.setStock(skuDto.getStock() != null ? skuDto.getStock() : 0);
        sku.setWarnStock(skuDto.getWarnStock() != null ? skuDto.getWarnStock() : 10);

        sku.setWeight(skuDto.getWeight());
        sku.setStatus(skuDto.getStatus() != null ? skuDto.getStatus() : 1);

        // 处理图片上传
        if (imageFile != null && !imageFile.isEmpty()) {
            Map<String, String> imageInfo = uploadImage(imageFile);
            if (imageInfo != null) {
                sku.setImage(imageInfo.get("relativePath"));
            } else {
                return Result.error("图片上传失败");
            }
        } else if (skuDto.getImage() != null) {
            sku.setImage(skuDto.getImage());
        }

        // 调用service新增SKU
        Long id = skuService.add(sku);
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("image", sku.getImage());
            return Result.success("添加成功", data);
        }
        return Result.error("添加失败");
    }

    /**
     * 批量新增SKU
     *
     * @param spuId SPU ID
     * @param skus  SKU列表的JSON字符串
     * @return 新增结果
     */
    @PostMapping("/batch-add")
    @PreAuthorize("hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> batchAdd(@RequestParam Long spuId,
                                 @RequestParam String skus) {
        List<SkuDto> skuDtoList = JSON.parseArray(skus, SkuDto.class);

        List<Sku> skusList = skuDtoList.stream()
                .map(dto -> {
                    Sku sku = new Sku();
                    sku.setSpuId(spuId);
                    sku.setPrice(dto.getPrice());
                    sku.setMarketPrice(dto.getMarketPrice());
                    sku.setCostPrice(dto.getCostPrice());
                    sku.setStock(dto.getStock() != null ? dto.getStock() : 0);
                    sku.setWarnStock(dto.getWarnStock() != null ? dto.getWarnStock() : 10);

                    sku.setWeight(dto.getWeight());
                    sku.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
                    sku.setImage(dto.getImage());
                    return sku;
                })
                .collect(Collectors.toList());

        boolean success = skuService.batchAdd(skusList);
        if (success) {
            return Result.success("批量添加成功", null);
        }
        return Result.error("批量添加失败");
    }

    /**
     * 删除SKU（逻辑删除）
     *
     * @param id SKU ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
    @PreAuthorize("hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
    @PreAuthorize("hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
     * 更新SKU信息（支持图片上传） =============似乎不需要
     *
     * @param skuDtoString SKU信息的JSON字符串
     * @param imageFile    上传的SKU图片（非必填）
     * @return 更新结果
     */
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Map<String, Object>> update(@RequestPart(value = "skuDto") String skuDtoString,
                                              @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        // 解析SKU DTO
        SkuDto skuDto = JSON.parseObject(skuDtoString, SkuDto.class);

        // 参数校验
        if (skuDto.getId() == null) {
            return Result.error("SKU ID不能为空");
        }

        // 查询原有SKU信息
        Sku existingSku = skuService.getById(skuDto.getId());
        if (existingSku == null) {
            return Result.error("SKU不存在");
        }

        // 创建SKU实体并设置更新后的值
        Sku sku = new Sku();
        sku.setId(skuDto.getId());

        if (skuDto.getPrice() != null) {
            sku.setPrice(skuDto.getPrice());
        }
        if (skuDto.getMarketPrice() != null) {
            sku.setMarketPrice(skuDto.getMarketPrice());
        }
        if (skuDto.getCostPrice() != null) {
            sku.setCostPrice(skuDto.getCostPrice());
        }
        if (skuDto.getStock() != null) {
            sku.setStock(skuDto.getStock());
        }
        if (skuDto.getWarnStock() != null) {
            sku.setWarnStock(skuDto.getWarnStock());
        }

        if (skuDto.getWeight() != null) {
            sku.setWeight(skuDto.getWeight());
        }
        if (skuDto.getStatus() != null) {
            sku.setStatus(skuDto.getStatus());
        }

        // 处理新上传的图片
        if (imageFile != null && !imageFile.isEmpty()) {
            // 删除旧图片
            if (StringUtils.hasText(existingSku.getImage())) {
                deleteImageFile(existingSku.getImage());
            }
            // 上传新图片
            Map<String, String> imageInfo = uploadImage(imageFile);
            if (imageInfo != null) {
                sku.setImage(imageInfo.get("relativePath"));
            } else {
                return Result.error("图片上传失败");
            }
        } else if (skuDto.getImage() != null) {
            // 使用指定的图片路径
            if (!skuDto.getImage().equals(existingSku.getImage())) {
                deleteImageFile(existingSku.getImage());
                sku.setImage(skuDto.getImage());
            } else {
                sku.setImage(existingSku.getImage());
            }
        } else {
            // 保留原有图片
            sku.setImage(existingSku.getImage());
        }

        // 调用service更新SKU
        boolean success = skuService.update(sku);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", sku.getId());
            data.put("image", sku.getImage());
            return Result.success("更新成功", data);
        }
        return Result.error("更新失败");
    }

    /**
     * 启用SKU
     *
     * @param id SKU ID
     * @return 启用结果
     */
    @PutMapping("/enable/{id}")
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
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
    @PreAuthorize("hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')")
    public Result<Void> decreaseStock(@RequestParam Long id,
                                      @RequestParam Integer quantity) {
        boolean success = skuService.decreaseStock(id, quantity);
        if (success) {
            return Result.success("扣减成功", null);
        }
        return Result.error("扣减失败，库存不足");
    }

    /**
     * 根据ID获取SKU详情
     *
     * @param id SKU ID
     * @return SKU信息
     */
    @GetMapping("/detail/{id}")
    public Result<Sku> getById(@PathVariable Long id) {
        Sku sku = skuService.getById(id);
        if (sku != null) {
            return Result.success(sku);
        }
        return Result.error("SKU不存在");
    }

    /**
     * 根据SPU ID获取SKU列表
     *
     * @param spuId SPU ID
     * @return SKU列表
     */
    @GetMapping("/list")
    public Result<List<Sku>> getBySpuId(@RequestParam Long spuId) {
        List<Sku> list = skuService.getBySpuId(spuId);
        return Result.success(list);
    }

    /**
     * 分页获取SKU列表==========暂时没有使用到
     *
     * @param spuId    SPU ID（可选）
     * @param status   状态（可选）
     * @param page     页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> getPage(@RequestParam(required = false) Long spuId,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Sku> list = skuService.getPage(spuId, status, page, pageSize);
        int total = skuService.count(spuId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("total", total);

        return Result.success(data);
    }

    /**
     * 获取SPU的最低价格==========暂时没有使用到
     *
     * @param spuId SPU ID
     * @return 最低价格
     */
    @GetMapping("/min-price/{spuId}")
    public Result<BigDecimal> getMinPrice(@PathVariable Long spuId) {
        BigDecimal minPrice = skuService.getMinPrice(spuId);
        return Result.success(minPrice);
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
     * 根据ID获取SKU详情（包含销售属性）
     *
     * @param id SKU ID
     * @return SKU详情（包含销售属性）
     */
    @GetMapping("/detail-with-attributes/{id}")
    public Result<SkuVo> getByIdWithAttributes(@PathVariable Long id) {
        SkuVo sku = skuService.getByIdWithAttributes(id);
        if (sku != null) {
            return Result.success(sku);
        }
        return Result.error("SKU不存在");
    }

    private Map<String, String> uploadImage(MultipartFile file) {
        Map<String, String> result = fileService.uploadImage(file, "sku");
        if (result != null) {
            Map<String, String> response = new HashMap<>();
            response.put("relativePath", result.get("relativePath"));
            response.put("imageUrl", "/uploads/images/sku/" + result.get("relativePath"));
            return response;
        }
        return null;
    }

    private void deleteImageFile(String image) {
        fileService.deleteFile(image, "sku");
    }
}
