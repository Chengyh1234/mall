package com.cyh.malluser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.malluser.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收货地址Mapper接口
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /**
     * 根据用户ID查询地址列表
     */
    List<Address> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询默认地址
     */
    Address selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询地址数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 取消用户所有默认地址
     */
    int cancelDefaultByUserId(@Param("userId") Long userId);
}