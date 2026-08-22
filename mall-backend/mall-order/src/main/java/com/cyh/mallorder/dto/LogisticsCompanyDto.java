package com.cyh.mallorder.dto;

/**
 * 物流公司 DTO
 * 封装物流公司信息的增删改查请求数据
 */
import com.cyh.mallcommon.validation.group.Create;
import com.cyh.mallcommon.validation.group.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物流公司 DTO
 *
 * <p>分组说明：
 * <ul>
 *   <li>{@link Create}：新增时校验——name、code 必填</li>
 *   <li>{@link Update}：修改时校验——id 必填</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsCompanyDto {

    /** 物流公司ID（更新时必传） */
    @NotNull(groups = Update.class, message = "物流公司ID不能为空")
    private Long id;

    /** 物流公司名称（如：顺丰速运） */
    @NotBlank(groups = Create.class, message = "物流公司名称不能为空")
    private String name;

    /** 物流公司代码（如：SF） */
    @NotBlank(groups = Create.class, message = "物流公司代码不能为空")
    private String code;

    /** 物流公司Logo */
    private String logo;

    /** 物流公司官网 */
    private String website;

    /** 物流公司客服电话 */
    private String phone;

    /** 排序（越小越靠前） */
    private Integer sort;

    /** 状态：1-启用 0-禁用 */
    private Integer status;
}