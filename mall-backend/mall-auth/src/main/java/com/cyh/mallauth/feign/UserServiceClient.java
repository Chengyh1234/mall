package com.cyh.mallauth.feign;

import com.cyh.mallcommon.dto.UserAuthDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务 Feign 客户端
 * <p>
 * 调用 mall-user 的内部接口获取用户认证数据，替代直连数据库。
 * 服务名 mall-user 通过 Nacos 服务发现解析。
 */
@FeignClient(name = "mall-user", path = "/api/internal/user")
public interface UserServiceClient {

    /**
     * 根据账号（用户名/手机号/邮箱）加载用户认证信息
     */
    @PostMapping("/loadByAccount")
    UserAuthDTO loadByAccount(@RequestParam("account") String account);

    /**
     * 根据邮箱加载用户认证信息
     */
    @GetMapping("/byEmail")
    UserAuthDTO loadByEmail(@RequestParam("email") String email);

    /**
     * 注册用户
     */
    @PostMapping("/register")
    UserAuthDTO register(@RequestBody RegisterRequest request);

    /**
     * 更新密码
     */
    @PutMapping("/password")
    boolean updatePassword(@RequestParam("userId") Long userId,
                           @RequestParam("encodedPassword") String encodedPassword);

    /**
     * 更新最后登录信息
     */
    @PutMapping("/lastLogin")
    void updateLastLogin(@RequestParam("userId") Long userId,
                         @RequestParam(value = "ip", required = false) String ip);

    /**
     * 检查邮箱是否已被注册
     */
    @GetMapping("/checkEmail")
    boolean checkEmail(@RequestParam("email") String email);

    /**
     * 检查用户名是否已被使用
     */
    @GetMapping("/checkUsername")
    boolean checkUsername(@RequestParam("username") String username);

    /**
     * 检查手机号是否已被注册
     */
    @GetMapping("/checkPhone")
    boolean checkPhone(@RequestParam("phone") String phone);

    @lombok.Data
    class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String realName;
    }
}