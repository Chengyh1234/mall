package com.cyh.mallportal.controller;

import cn.hutool.core.util.IdUtil;
import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.validation.Password;
import com.cyh.mallcommon.validation.Phone;
import com.cyh.mallcommon.validation.Username;
import com.cyh.mallportal.entity.Role;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.entity.UserRole;
import com.cyh.mallportal.mapper.RoleMapper;
import com.cyh.mallportal.mapper.UserMapper;
import com.cyh.mallportal.mapper.UserRoleMapper;
import com.cyh.mallportal.service.EmailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器 已处理响应
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
    private final RoleMapper roleMapper;
    private final EmailService emailService;
    private final UserRoleMapper userRoleMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录接口（仅限普通用户角色）
     * <p>
     * 需要先调用 GET /captcha 获取验证码，然后在登录时提交 captchaKey 和 captcha。
     * 登录成功后，会校验当前用户是否拥有 USER 角色，若无则拒绝登录。
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest request) {
        // ========== 图形验证码校验 ==========
        // 从 Redis 中获取存储的验证码文本
        String redisKey = MyConstants.CAPTCHA_PREFIX + request.getCaptchaKey();
        String storedCaptcha = (String) redisTemplate.opsForValue().get(redisKey);

        // 验证码不存在或已过期
        if (!StringUtils.hasText(storedCaptcha)) {
            return Result.error("验证码已过期，请重新获取");
        }

        // 忽略大小写比较验证码
        if (!storedCaptcha.equalsIgnoreCase(request.getCaptcha())) {
            return Result.error("验证码错误");
        }

        // 校验通过后立即删除该验证码（一次性使用，防止重复提交）
        redisTemplate.delete(redisKey);
        // ========== 验证码校验结束 ==========


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getAccount(), request.getPassword())//创建认证对象
        );

        User user = (User) authentication.getPrincipal();

        // 校验用户状态：被禁用的用户拒绝登录
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，无法登录");//无法进行使用，需要使用security体系的异常
        }

        // ========== 角色校验：仅允许普通用户(USER)角色登录 ==========
        boolean hasUserRole = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> "USER".equals(role.getCode()));
        // 允许同时拥有 USER 和 SELLER 角色的用户登录前台
        if (!hasUserRole) {
            return Result.error("该账号无普通用户权限，请使用管理员登录入口");
        }

        String token = IdUtil.fastSimpleUUID();
        String sessionId = IdUtil.fastSimpleUUID();

        // 单点登录：使旧会话失效
        // 存储用户当前会话
        redisTemplate.opsForValue().set(
                MyConstants.USER_CURRENT_SESSION_PREFIX + user.getId(),
                sessionId,
                MyConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );

        // 存储 userId → token 反向映射，用于权限变更时原地更新 Redis 缓存
        redisTemplate.opsForValue().set(
                MyConstants.USER_ACTIVE_TOKEN_PREFIX + user.getId(),
                token,
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
        userInfo.put("status", user.getStatus()); // 记录用户启用/禁用状态，供TokenAuthenticationFilter校验

        Map<String, String> rolesMap = new HashMap<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                rolesMap.put(role.getCode(), role.getName());
            }
        }
        userInfo.put("roles", rolesMap);

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
        data.put("roles", user.getRoles().stream().map(role -> role.getCode()).toList());

        return Result.success("登录成功", data);
    }

    /**
     * 管理员登录接口（仅限运营管理员和超级管理员）
     * <p>
     * 与用户登录接口共用相同的验证码流程和 Token 生成逻辑，
     * 但登录成功后额外校验角色：仅允许 ADMIN 或 SUPER_ADMIN 角色登录。
     * </p>
     * <p>
     * 需要先调用 GET /captcha 获取验证码，然后在登录时提交 captchaKey 和 captcha。
     */
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@RequestBody @Valid LoginRequest request) {
        // ========== 图形验证码校验 ==========
        String redisKey = MyConstants.CAPTCHA_PREFIX + request.getCaptchaKey();
        String storedCaptcha = (String) redisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            return Result.error("验证码已过期，请重新获取");
        }

        if (!storedCaptcha.equalsIgnoreCase(request.getCaptcha())) {
            return Result.error("验证码错误");
        }

        redisTemplate.delete(redisKey);
        // ========== 验证码校验结束 ==========

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getAccount(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        // 校验用户状态：被禁用的用户拒绝登录
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，无法登录");//无法进行使用，需要使用security体系的异常
        }

        // ========== 角色校验：仅允许超级管理员(SUPER_ADMIN)角色登录 ==========
        boolean hasAdminRole = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getCode()));
        if (!hasAdminRole) {
            return Result.error("该账号无管理员权限，请使用普通用户登录入口");
        }

        String token = IdUtil.fastSimpleUUID();
        String sessionId = IdUtil.fastSimpleUUID();

        // 单点登录：使旧会话失效
        // 存储用户当前会话
        redisTemplate.opsForValue().set(
                MyConstants.USER_CURRENT_SESSION_PREFIX + user.getId(),
                sessionId,
                MyConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );

        // 存储 userId → token 反向映射，用于权限变更时原地更新 Redis 缓存
        redisTemplate.opsForValue().set(
                MyConstants.USER_ACTIVE_TOKEN_PREFIX + user.getId(),
                token,
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
        userInfo.put("status", user.getStatus()); // 记录用户启用/禁用状态，供TokenAuthenticationFilter校验

        Map<String, String> rolesMap = new HashMap<>();
        if (user.getRoles() != null) {
            for (var role : user.getRoles()) {
                rolesMap.put(role.getCode(), role.getName());
            }
        }
        userInfo.put("roles", rolesMap);

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
        data.put("roles", user.getRoles().stream().map(role -> role.getCode()).toList());

        return Result.success("管理员登录成功", data);
    }



    /**
     * 发送注册邮箱验证码
     * <p>
     * 用户输入邮箱后，先校验图形验证码（防批量调用），
     * 然后生成6位随机验证码发送至该邮箱，验证码存入 Redis，有效期5分钟。
     */
    @PostMapping("/register/send-email-code")
    public Result<String> sendRegisterEmailCode(@RequestBody @Valid SendRegisterEmailCodeRequest request) {
        // ========== 图形验证码校验（防批量调用） ==========
        String captchaKey = request.getCaptchaKey();
        String captcha = request.getCaptcha();
        String storedCaptcha = (String) redisTemplate.opsForValue().get(MyConstants.CAPTCHA_PREFIX + captchaKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            return Result.error("图形验证码已过期，请重新获取");
        }

        if (!storedCaptcha.equalsIgnoreCase(captcha)) {
            return Result.error("图形验证码错误");
        }

        redisTemplate.delete(MyConstants.CAPTCHA_PREFIX + captchaKey);
        // ========== 图形验证码校验结束 ==========

        // 1. 检查邮箱是否已被注册
        String email = request.getEmail();
        User existingUser = userMapper.selectByEmail(email);
        if (existingUser != null) {
            return Result.error("该邮箱已被注册");
        }

        // 2. 检查是否已发送过验证码（防止短时间内重复发送）
        String redisKey = MyConstants.EMAIL_REGISTER_CODE_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            return Result.error("验证码已发送，请查看邮箱或稍后再试");
        }

        // 3. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 4. 发送邮件
        emailService.sendRegisterCode(email, code);

        // 5. 发送成功后存入 Redis，有效期分1钟
        redisTemplate.opsForValue().set(redisKey, code, MyConstants.EMAIL_CODE_EXPIRATION, TimeUnit.SECONDS);

        return Result.success("验证码已发送", null);
    }

    /**
     * 注册接口（邮箱验证码校验）
     * <p>
     * 注册流程：邮箱验证码校验 → 唯一性校验 → 密码加密 → 插入用户 → 自动分配 USER 角色。
     * 注册成功后可直接登录。
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Valid RegisterRequest request) {
        // 1. 邮箱验证码校验
        String redisKey = MyConstants.EMAIL_REGISTER_CODE_PREFIX + request.getEmail();
        String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(storedCode)) {
            return Result.error("验证码已过期，请重新获取");
        }

        if (!storedCode.equals(request.getEmailCode())) {
            return Result.error("验证码错误");
        }

        // 验证通过后立即删除验证码（一次性使用）
        redisTemplate.delete(redisKey);

        // 2. 用户名唯一性校验
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            return Result.error("用户名已存在");
        }

        // 3. 手机号唯一性校验（如果提供了手机号）
        if (StringUtils.hasText(request.getPhone())) {
            User phoneUser = userMapper.selectByPhone(request.getPhone());
            if (phoneUser != null) {
                return Result.error("该手机号已被注册");
            }
        }

        // 4. 邮箱唯一性校验（双重保险）
        User emailUser = userMapper.selectByEmail(request.getEmail());
        if (emailUser != null) {
            return Result.error("该邮箱已被注册");
        }

        // 5. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRealName(request.getRealName());
        user.setStatus(1);

        userMapper.insert(user);

        // 6. 自动分配 USER 角色（注册后可直接登录）
        com.cyh.mallportal.entity.Role userRole = roleMapper.selectByCode("USER");
        if (userRole != null) {
            UserRole userRoleEntity = new UserRole();
            userRoleEntity.setUserId(user.getId());
            userRoleEntity.setRoleId(userRole.getId());
            userRoleMapper.insert(userRoleEntity);
            log.info("用户 {} 注册成功，已自动分配 USER 角色", user.getUsername());
        } else {
            log.warn("USER 角色不存在，请检查数据库 roles 表");
        }

        return Result.success("注册成功", null);
    }

    /**
     * 发送邮箱登录验证码
     * <p>
     * 用户输入邮箱后，系统生成6位随机验证码发送至该邮箱，
     * 验证码存入 Redis，有效期5分钟。
     */
    @PostMapping("/login/send-email-code")
    public Result<String> sendLoginEmailCode(@RequestBody @Valid SendEmailCodeRequest request) {
        // ========== 图形验证码校验（防批量调用） ==========
        String captchaKey = request.getCaptchaKey();
        String captcha = request.getCaptcha();
        String storedCaptcha = (String) redisTemplate.opsForValue().get(MyConstants.CAPTCHA_PREFIX + captchaKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            return Result.error("图形验证码已过期，请重新获取");
        }

        if (!storedCaptcha.equalsIgnoreCase(captcha)) {
            return Result.error("图形验证码错误");
        }

        redisTemplate.delete(MyConstants.CAPTCHA_PREFIX + captchaKey);
        // ========== 图形验证码校验结束 ==========

        // 1. 确认该邮箱已注册
        String email = request.getEmail();
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return Result.error("该邮箱未注册");
        }

        // 2. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 3. 发送邮件
        String redisKey = MyConstants.EMAIL_LOGIN_CODE_PREFIX + email;
        emailService.sendLoginCode(email, code);

        // 4. 发送成功后存入 Redis，有效期1分钟
        redisTemplate.opsForValue().set(redisKey, code, MyConstants.EMAIL_CODE_EXPIRATION, TimeUnit.SECONDS);

        return Result.success("验证码已发送", null);
    }

    /**
     * 用邮箱验证码登录（仅限普通用户）
     * <p>
     * 使用邮箱 + 验证码进行登录，无需密码。
     * 登录成功后校验 USER 角色。
     */
    @PostMapping("/login/email-code")
    public Result<Map<String, Object>> loginByEmailCode(@RequestBody @Valid EmailCodeLoginRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        // 1. 从 Redis 取出验证码并比对
        String redisKey = MyConstants.EMAIL_LOGIN_CODE_PREFIX + email;
        String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(storedCode)) {
            return Result.error("验证码已过期，请重新获取");
        }

        if (!storedCode.equals(code)) {
            return Result.error("验证码错误");
        }

        // 验证通过后立即删除验证码（一次性使用）
        redisTemplate.delete(redisKey);

        // 2. 查询用户
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 校验用户状态：被禁用的用户拒绝登录
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，无法登录");//无法进行使用，需要使用security体系的异常
        }

        // 3. 加载角色
        var roles = roleMapper.selectByUserId(user.getId());
        user.setRoles(roles);
        // 只构建角色权限（不加具体权限编码）
        var authorities = new java.util.ArrayList<GrantedAuthority>();
        for (var role : roles) {
            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.getCode()));
        }
        user.setAuthorities(authorities);

        // 4. 角色校验：仅允许普通用户(USER)角色使用邮箱验证码登录
        boolean hasUserRole = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> "USER".equals(role.getCode()));
        // 允许同时拥有 USER 和 SELLER 角色的用户登录前台
        if (!hasUserRole) {
            return Result.error("该账号无普通用户权限，请使用管理员登录入口");
        }

        // 校验用户状态：被禁用的用户不允许登录
        if (user.getStatus() == null || user.getStatus() != 1) {
            return Result.error("账号已被禁用，无法登录");
        }

        // 5. 生成 Token
        String token = IdUtil.fastSimpleUUID();
        String sessionId = IdUtil.fastSimpleUUID();

        // 单点登录：使旧会话失效
        redisTemplate.opsForValue().set(
                MyConstants.USER_CURRENT_SESSION_PREFIX + user.getId(),
                sessionId,
                MyConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );
        // 存储 userId → token 反向映射，用于权限变更时原地更新 Redis 缓存
        redisTemplate.opsForValue().set(
                MyConstants.USER_ACTIVE_TOKEN_PREFIX + user.getId(),
                token,
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
        userInfo.put("status", user.getStatus()); // 记录用户启用/禁用状态，供TokenAuthenticationFilter校验

        Map<String, String> rolesMap = new HashMap<>();
        if (user.getRoles() != null) {
            for (var role : user.getRoles()) {
                rolesMap.put(role.getCode(), role.getName());
            }
        }
        userInfo.put("roles", rolesMap);

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
        data.put("roles", user.getRoles().stream().map(role -> role.getCode()).toList());

        return Result.success("登录成功", data);
    }

    /**
     * 发送重置密码验证码
     * <p>
     * 用户输入已注册的邮箱，系统发送重置密码验证码。
     * 验证码存入 Redis，有效期5分钟。
     */
    @PostMapping("/reset-password/send-code")
    public Result<String> sendResetPasswordCode(@RequestBody @Valid SendEmailCodeRequest request) {
        // ========== 图形验证码校验（防批量调用） ==========
        String captchaKey = request.getCaptchaKey();
        String captcha = request.getCaptcha();
        String storedCaptcha = (String) redisTemplate.opsForValue().get(MyConstants.CAPTCHA_PREFIX + captchaKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            return Result.error("图形验证码已过期，请重新获取");
        }

        if (!storedCaptcha.equalsIgnoreCase(captcha)) {
            return Result.error("图形验证码错误");
        }

        redisTemplate.delete(MyConstants.CAPTCHA_PREFIX + captchaKey);
        // ========== 图形验证码校验结束 ==========

        // 1. 确认该邮箱已注册
        String email = request.getEmail();
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return Result.error("该邮箱未注册");
        }

        // 2. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 3. 发送邮件
        String redisKey = MyConstants.EMAIL_RESET_PWD_CODE_PREFIX + email;
        emailService.sendResetPasswordCode(email, code);

        // 4. 发送成功后存入 Redis，有效期1分钟
        redisTemplate.opsForValue().set(redisKey, code, MyConstants.EMAIL_CODE_EXPIRATION, TimeUnit.SECONDS);

        return Result.success("验证码已发送", null);
    }

    /**
     * 重置密码
     * <p>
     * 校验邮箱验证码后，使用 BCrypt 加密新密码并更新到数据库。
     * 重置成功后清除该用户所有登录会话，需重新登录。
     */
    @PostMapping("/reset-password/reset")
    public Result<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        // 1. 从 Redis 取出验证码并比对
        String redisKey = MyConstants.EMAIL_RESET_PWD_CODE_PREFIX + email;
        String storedCode = (String) redisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(storedCode)) {
            return Result.error("验证码已过期，请重新获取");
        }

        if (!storedCode.equals(code)) {
            return Result.error("验证码错误");
        }

        // 验证通过后立即删除验证码（一次性使用）
        redisTemplate.delete(redisKey);

        // 2. 查询用户
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 3. BCrypt 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 4. 更新密码
        user.setPassword(encodedPassword);
        userMapper.updateById(user);

        // 5. 清除该用户所有登录会话（强制重新登录）
        // 清除当前会话
        redisTemplate.delete(MyConstants.USER_CURRENT_SESSION_PREFIX + user.getId());
        // 清除 Token（通过模糊匹配删除所有以 token: 开头且包含该 userId 的 key）
        // 注：由于 Redis 的 key 结构限制，此处清理当前会话即可，
        // 旧的 token 会在过期后自动失效，或由客户端在下次请求时被过滤器拦截

        log.info("用户 {} 密码重置成功，已清除登录会话", user.getUsername());
        return Result.success("密码重置成功，请使用新密码重新登录", null);
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> logout(@RequestHeader(value = MyConstants.AUTH_HEADER, required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith(MyConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(7);

            // 获取用户信息，清除用户当前会话和反向映射
            Object userInfoObj = redisTemplate.opsForValue().get(MyConstants.TOKEN_PREFIX + token);
            if (userInfoObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;
                Long userId = ((Number) userInfo.get("userId")).longValue();
                redisTemplate.delete(MyConstants.USER_CURRENT_SESSION_PREFIX + userId);
                redisTemplate.delete(MyConstants.USER_ACTIVE_TOKEN_PREFIX + userId);
            }

            redisTemplate.delete(MyConstants.TOKEN_PREFIX + token);
        }
        SecurityContextHolder.clearContext();
        return Result.success("登出成功", null);
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
        /** 账号（用户名/手机号/邮箱 三选一） */
        @NotBlank(message = "账号不能为空")
        private String account;
        /** 密码（6-20位） */
        @Password
        private String password;
        /** 验证码唯一标识（从 GET /captcha 接口获取） */
        @NotBlank(message = "验证码标识不能为空")
        private String captchaKey;
        /** 用户输入的验证码内容 */
        @NotBlank(message = "验证码不能为空")
        private String captcha;
        private String ip;
    }

    @Data
    public static class RegisterRequest {
        /** 用户名（4-20位，必须包含字母和数字，不能含特殊符号） */
        @Username
        private String username;
        /** 密码（6-20位） */
        @Password
        private String password;
        /** 邮箱地址 */
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        /** 邮箱验证码（从 /auth/register/send-email-code 获取） */
        @NotBlank(message = "邮箱验证码不能为空")
        private String emailCode;
        /** 手机号（11位，1[3-9]开头） */
        @Phone
        private String phone;
        private String realName;
    }

    /** 发送注册邮箱验证码请求 */
    @Data
    public static class SendRegisterEmailCodeRequest {
        /** 邮箱地址 */
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        /** 图形验证码唯一标识 */
        @NotBlank(message = "验证码标识不能为空")
        private String captchaKey;
        /** 图形验证码内容 */
        @NotBlank(message = "验证码不能为空")
        private String captcha;
    }

    /** 发送邮箱验证码请求 */
    @Data
    public static class SendEmailCodeRequest {
        /** 邮箱地址 */
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        /** 图形验证码唯一标识 */
        @NotBlank(message = "验证码标识不能为空")
        private String captchaKey;
        /** 图形验证码内容 */
        @NotBlank(message = "验证码不能为空")
        private String captcha;
    }

    /** 邮箱验证码登录请求 */
    @Data
    public static class EmailCodeLoginRequest {
        /** 邮箱地址 */
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        /** 6位验证码 */
        @NotBlank(message = "验证码不能为空")
        private String code;
        /** 客户端IP */
        private String ip;
    }

    /** 重置密码请求 */
    @Data
    public static class ResetPasswordRequest {
        /** 邮箱地址 */
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        /** 6位验证码 */
        @NotBlank(message = "验证码不能为空")
        private String code;
        /** 新密码（6-20位） */
        @Password
        private String newPassword;
    }
}
