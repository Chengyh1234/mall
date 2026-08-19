package com.cyh.mallauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallauth.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查询角色
     *
     * @param code 角色编码，如 USER、ADMIN
     * @return 角色信息
     */
    Role selectByCode(@Param("code") String code);

    }