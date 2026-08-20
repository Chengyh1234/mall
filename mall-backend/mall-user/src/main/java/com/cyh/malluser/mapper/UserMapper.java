package com.cyh.malluser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.malluser.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户信息
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 统一账号查询：支持用户名/手机号/邮箱 三选一登录
     */
    User selectByAccount(@Param("account") String account);

    /**
     * 根据手机号查询用户
     */
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}