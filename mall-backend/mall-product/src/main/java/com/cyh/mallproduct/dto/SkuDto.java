package com.cyh.mallproduct.dto;

import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * SKU 新增/修改 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——spuId、price 必填</li>
 *   <li>{@link Update}：修改时校验——id 必填</li>
 * </ul>
 * <p>图片处理：
 * <ul>
 *   <li>imageFile：新上传的 SKU 图片文件</li>
 *   <li>image：已有 SKU 图片路径（修改时保留旧图）</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuDto {

    /** 主键ID（修改时必传） */
    @NotNull(groups = Update.class, message = "SKU ID不能为空")
    private Long id;

    /** 所属 SPU ID（新增时必填） */
    @NotNull(groups = Create.class, message = "所属商品ID不能为空")
    private Long spuId;

    /** 销售价格 */
    @NotNull(groups = Create.class, message = "销售价格不能为空")
    private BigDecimal price;

    /** 市场价 */
    private BigDecimal marketPrice;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 库存数量 */
    private Integer stock;

    /** 预警库存 */
    private Integer warnStock;

    /** 已有 SKU 图片路径 */
    private String image;

    /** 重量(kg) */
    private BigDecimal weight;

    /** 状态：1-启用 0-禁用 */
    private Integer status;

    /** 新上传的 SKU 图片文件 */
    private MultipartFile imageFile;
}