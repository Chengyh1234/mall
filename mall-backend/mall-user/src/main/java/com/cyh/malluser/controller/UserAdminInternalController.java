package com.cyh.malluser.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.malluser.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理内部接口控制器（供管理员仪表盘 Feign 调用）
 */
@RestController
@RequestMapping("/internal/user/admin")
@RequiredArgsConstructor
public class UserAdminInternalController {

    private final UserMapper userMapper;

    /**
     * 获取平台总用户数
     */
    @GetMapping("/user-count")
    public Result<Long> getUserCount() {
        return Result.success(userMapper.selectCount(null));
    }
}