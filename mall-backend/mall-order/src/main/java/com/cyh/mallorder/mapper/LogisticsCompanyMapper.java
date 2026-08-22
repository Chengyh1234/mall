package com.cyh.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallorder.entity.LogisticsCompany;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物流公司数据访问层
 */
@Mapper
public interface LogisticsCompanyMapper extends BaseMapper<LogisticsCompany> {

    /**
     * 获取所有启用的物流公司列表
     */
    List<LogisticsCompany> selectEnabledList();

    /**
     * 根据代码查询物流公司
     */
    LogisticsCompany selectByCode(@Param("code") String code);
}