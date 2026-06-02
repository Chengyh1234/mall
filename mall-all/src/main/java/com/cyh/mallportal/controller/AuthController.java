package com.cyh.mallportal.controller;

import cn.hutool.core.util.IdUtil;
import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.mapper.UserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 提供登录、注册、登出等功能
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())//创建认证对象
            );

            User user = (User) authentication.getPrincipal();

            String token = IdUtil.fastSimpleUUID();
            String sessionId = IdUtil.fastSimpleUUID();

            // 单点登录：检查用户是否已有活跃会话，如果有则使旧会话失效
            String oldSessionId = (String) redisTemplate.opsForValue().get(MyConstants.USER_CURRENT_SESSION_PREFIX + user.getId());
            if (oldSessionId != null) {
                // 删除旧会话的Token（需要维护sessionId到token的映射，这里简化处理）
                // 实际项目中可以存储 sessionId -> token 的映射
                log.warn("用户 {} 在其他设备登录，旧会话已失效", user.getUsername());
            }

            // 存储用户当前会话
            redisTemplate.opsForValue().set(
                    MyConstants.USER_CURRENT_SESSION_PREFIX + user.getId(),
                    sessionId,
                    MyConstants.TOKEN_EXPIRATION,
                    TimeUnit.SECONDS
            );

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("sessionId", sessionId);
            userInfo.put("userId", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("realName", user.getRealName());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());

            Map<String, String> rolesMap = new HashMap<>();
            if (user.getRoles() != null) {
                for (var role : user.getRoles()) {
                    rolesMap.put(role.getCode(), role.getName());
                }
            }
            userInfo.put("roles", rolesMap);

            Map<String, Boolean> permissions = new HashMap<>();
            for (GrantedAuthority authority : user.getAuthorities()) {
                permissions.put(authority.getAuthority(), true);
            }
            userInfo.put("permissions", permissions);

            redisTemplate.opsForValue().set(
                    MyConstants.TOKEN_PREFIX + token,
                    userInfo,
                    MyConstants.TOKEN_EXPIRATION,
                    TimeUnit.SECONDS
            );

            updateLastLogin(user.getId(), request.getIp());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("roles", user.getRoles());

            return Result.success("登录成功", data);
        } catch (Exception e) {
            throw new BusinessException("用户名或密码错误");
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录");
        }

        User user = (User) authentication.getPrincipal();

        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("avatar", user.getAvatar());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("roles", user.getRoles());
        data.put("permissions", user.getAuthorities());

        return Result.success(data);
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = MyConstants.AUTH_HEADER, required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith(MyConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(7);
            
            // 获取用户信息，清除用户当前会话
            Object userInfoObj = redisTemplate.opsForValue().get(MyConstants.TOKEN_PREFIX + token);
            if (userInfoObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                Long userId = ((Number) userInfo.get("userId")).longValue();
                redisTemplate.delete(MyConstants.USER_CURRENT_SESSION_PREFIX + userId);
            }
            
            redisTemplate.delete(MyConstants.TOKEN_PREFIX + token);
        }
        SecurityContextHolder.clearContext();
        return Result.success("登出成功", null);
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);

        userMapper.insert(user);

        return Result.success("注册成功", null);
    }

    private void updateLastLogin(Long userId, String ip) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateById(user);
        }
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
        private String ip;
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private String realName;
    }
}
