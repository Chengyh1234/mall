package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.LogisticsCompany;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流公司Mapper接口
 * 提供物流公司数据访问操作
 */
@Mapper
public interface LogisticsCompanyMapper extends BaseMapper<LogisticsCompany> {

    /**
     * 获取所有启用的物流公司列表（按排序）
     *
     * @return 物流公司列表
     */
    List<LogisticsCompany> selectEnabledList();

    /**
     * 根据代码获取物流公司
     *
     * @param code 物流公司代码
     * @return 物流公司
     */
    LogisticsCompany selectByCode(@Param("code") String code);
}