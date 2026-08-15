package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.LogisticsCompany;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流公司管理 VO
 *
 * 管理后台专用，包含完整管理字段
 * 对应接口：
 *   GET /logistics/page
 */
@Data
public class LogisticsCompanyAdminVo {

    /** 物流公司ID */
    private Long id;

    /** 物流公司名称（如：顺丰速运） */
    private String name;

    /** 物流公司代码（如：SF） */
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

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public static LogisticsCompanyAdminVo fromLogisticsCompany(LogisticsCompany company) {
        LogisticsCompanyAdminVo vo = new LogisticsCompanyAdminVo();
        vo.setId(company.getId());
        vo.setName(company.getName());
        vo.setCode(company.getCode());
        vo.setLogo(company.getLogo());
        vo.setWebsite(company.getWebsite());
        vo.setPhone(company.getPhone());
        vo.setSort(company.getSort());
        vo.setStatus(company.getStatus());
        vo.setCreatedAt(company.getCreatedAt());
        vo.setUpdatedAt(company.getUpdatedAt());
        return vo;
    }
}