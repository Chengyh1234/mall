package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.LogisticsCompany;
import lombok.Data;

/**
 * 物流公司 VO
 *
 * 前端公开接口专用，排除 sort/status/createdAt/updatedAt 等内部管理字段
 * 对应接口：
 *   GET /logistics/list
 *   GET /logistics/detail/{id}
 *   GET /logistics/code/{code}
 */
@Data
public class LogisticsCompanyVo {

    /** 物流公司ID */
    private Long id;

    /** 物流公司名称（如：顺丰速运） */
    private String name;

    /** 物流公司代码（如：SF） */
    private String code;

    /** 物流公司Logo */
    private String logo;

    /** 物流公司客服电话 */
    private String phone;

    public static LogisticsCompanyVo fromLogisticsCompany(LogisticsCompany company) {
        LogisticsCompanyVo vo = new LogisticsCompanyVo();
        vo.setId(company.getId());
        vo.setName(company.getName());
        vo.setCode(company.getCode());
        vo.setLogo(company.getLogo());
        vo.setPhone(company.getPhone());
        return vo;
    }
}