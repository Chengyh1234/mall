package com.cyh.malluser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.malluser.entity.Role;
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
     */
    List<Role> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色编码查询角色
     */
    Role selectByCode(@Param("code") String code);

    /**
     * 根据角色ID查询权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}