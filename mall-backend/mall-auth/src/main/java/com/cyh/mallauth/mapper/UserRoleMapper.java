package com.cyh.mallauth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallauth.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper 接口
 *
 * 提供用户与角色绑定关系的数据访问
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}