package com.cyh.malluser.controller;

import com.cyh.mallcommon.dto.UserAuthDTO;
import com.cyh.malluser.entity.User;
import com.cyh.malluser.service.UserInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户内部 API 控制器（供其他微服务通过 Feign 调用，不对外暴露）
 * <p>
 * 请求路径 /api/internal/** 不通过网关路由，仅由 Feign 服务间直接调用。
 * 提供通用的用户数据操作，如查询、注册、更新密码等。
 */
@Slf4j
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserInternalService userInternalService;

    /**
     * 根据账号（用户名/手机号/邮箱）加载用户认证信息
     */
    @PostMapping("/loadByAccount")
    public UserAuthDTO loadByAccount(@RequestParam("account") String account) {
        return userInternalService.loadUserByAccount(account);
    }

    /**
     * 根据邮箱加载用户认证信息
     */
    @GetMapping("/byEmail")
    public UserAuthDTO loadByEmail(@RequestParam("email") String email) {
        return userInternalService.loadUserByEmail(email);
    }

    /**
     * 注册用户
     */
    @PostMapping("/register")
    public UserAuthDTO register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);
        return userInternalService.registerUser(user);
    }

    /**
     * 更新密码
     */
    @PutMapping("/password")
    public boolean updatePassword(@RequestParam("userId") Long userId,
                                  @RequestParam("encodedPassword") String encodedPassword) {
        return userInternalService.updatePassword(userId, encodedPassword);
    }

    /**
     * 更新最后登录信息
     */
    @PutMapping("/lastLogin")
    public void updateLastLogin(@RequestParam("userId") Long userId,
                                @RequestParam(value = "ip", required = false) String ip) {
        userInternalService.updateLastLogin(userId, ip);
    }

    /**
     * 检查邮箱是否已被注册
     */
    @GetMapping("/checkEmail")
    public boolean checkEmail(@RequestParam("email") String email) {
        return userInternalService.isEmailExists(email);
    }

    /**
     * 检查用户名是否已被使用
     */
    @GetMapping("/checkUsername")
    public boolean checkUsername(@RequestParam("username") String username) {
        return userInternalService.isUsernameExists(username);
    }

    /**
     * 检查手机号是否已被注册
     */
    @GetMapping("/checkPhone")
    public boolean checkPhone(@RequestParam("phone") String phone) {
        return userInternalService.isPhoneExists(phone);
    }

    @lombok.Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String realName;
    }
}