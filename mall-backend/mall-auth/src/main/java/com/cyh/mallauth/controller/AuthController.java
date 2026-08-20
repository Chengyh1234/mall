package com.cyh.mallauth.controller;

import cn.hutool.core.util.IdUtil;
import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.dto.RoleDTO;
import com.cyh.mallcommon.dto.UserAuthDTO;
import com.cyh.mallcommon.exception.CaptchaInvalidException;
import com.cyh.mallcommon.exception.ErrorCode;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.validation.Password;
import com.cyh.mallcommon.validation.Phone;
import com.cyh.mallcommon.validation.Username;
import com.cyh.mallauth.entity.Role;
import com.cyh.mallauth.entity.User;
import com.cyh.mallauth.feign.UserServiceClient;
import com.cyh.mallauth.mq.event.EmailSendEvent;
import com.cyh.mallauth.mq.publisher.EmailEventPublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证控制器 已处理响应
 * 提供登录、注册、登出等功能
 * <p>
 * 用户数据通过 Feign 调用 mall-user 获取，不再直连数据库。
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    private final UserServiceClient userServiceClient;
    private final EmailEventPublisher emailEventPublisher;

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
        String redisKey = RedisConstants.CAPTCHA_PREFIX + request.getCaptchaKey();
        String storedCaptcha = redisUtils.get(redisKey);

        // 验证码不存在或已过期
        if (!StringUtils.hasText(storedCaptcha)) {
            throw new CaptchaInvalidException();
        }

        // 忽略大小写比较验证码
        if (!storedCaptcha.equalsIgnoreCase(request.getCaptcha())) {
            return Result.error("验证码错误");
        }

        // 校验通过后立即删除该验证码（一次性使用，防止重复提交）
        redisUtils.delete(redisKey);
        // ========== 验证码校验结束 ==========


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getAccount(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        // 校验用户状态：被禁用的用户拒绝登录
        if (user.getStatus() == null || user.getStatus() != 1) {
            return Result.error("账号已被禁用，无法登录");
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
        redisUtils.set(
                RedisConstants.USER_CURRENT_SESSION_PREFIX + user.getId(),
                sessionId,
                RedisConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );

        redisUtils.set(
                RedisConstants.USER_ACTIVE_TOKEN_PREFIX + user.getId(),
                token,
                RedisConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sessionId", sessionId);
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("status", user.getStatus());

        Map<String, String> rolesMap = new HashMap<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                rolesMap.put(role.getCode(), role.getName());
            }
        }
        userInfo.put("roles", rolesMap);

        redisUtils.setObject(
                RedisConstants.TOKEN_PREFIX + token,
                userInfo,
                RedisConstants.TOKEN_EXPIRATION,
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
     */
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@RequestBody @Valid LoginRequest request) {
        // ========== 图形验证码校验 ==========
        String redisKey = RedisConstants.CAPTCHA_PREFIX + request.getCaptchaKey();
        String storedCaptcha = redisUtils.get(redisKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            throw new CaptchaInvalidException();
        }

        if (!storedCaptcha.equalsIgnoreCase(request.getCaptcha())) {
            return Result.error("验证码错误");
        }

        redisUtils.delete(redisKey);
        // ========== 验证码校验结束 ==========

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getAccount(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        // 校验用户状态：被禁用的用户拒绝登录
        if (user.getStatus() == null || user.getStatus() != 1) {
            return Result.error("账号已被禁用，无法登录");
        }

        // ========== 角色校验：仅允许超级管理员(SUPER_ADMIN)角色登录 ==========
        boolean hasAdminRole = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getCode()));
        if (!hasAdminRole) {
            return Result.error("该账号无管理员权限，请使用普通用户登录入口");
        }

        String token = IdUtil.fastSimpleUUID();
        String sessionId = IdUtil.fastSimpleUUID();

        redisUtils.set(
                RedisConstants.USER_CURRENT_SESSION_PREFIX + user.getId(),
                sessionId,
                RedisConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );

        redisUtils.set(
                RedisConstants.USER_ACTIVE_TOKEN_PREFIX + user.getId(),
                token,
                RedisConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sessionId", sessionId);
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("status", user.getStatus());

        Map<String, String> rolesMap = new HashMap<>();
        if (user.getRoles() != null) {
            for (var role : user.getRoles()) {
                rolesMap.put(role.getCode(), role.getName());
            }
        }
        userInfo.put("roles", rolesMap);

        redisUtils.setObject(
                RedisConstants.TOKEN_PREFIX + token,
                userInfo,
                RedisConstants.TOKEN_EXPIRATION,
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
     */
    @PostMapping("/register/send-email-code")
    public Result<String> sendRegisterEmailCode(@RequestBody @Valid SendRegisterEmailCodeRequest request) {
        // ========== 图形验证码校验（防批量调用） ==========
        String captchaKey = request.getCaptchaKey();
        String captcha = request.getCaptcha();
        String storedCaptcha = redisUtils.get(RedisConstants.CAPTCHA_PREFIX + captchaKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            throw new CaptchaInvalidException();
        }

        if (!storedCaptcha.equalsIgnoreCase(captcha)) {
            return Result.error("图形验证码错误");
        }

        redisUtils.delete(RedisConstants.CAPTCHA_PREFIX + captchaKey);
        // ========== 图形验证码校验结束 ==========

        // 1. 检查邮箱是否已被注册
        String email = request.getEmail();
        if (userServiceClient.checkEmail(email)) {
            return Result.error("该邮箱已被注册");
        }

        // 2. 检查是否已发送过验证码
        String redisKey = RedisConstants.EMAIL_REGISTER_CODE_PREFIX + email;
        if (Boolean.TRUE.equals(redisUtils.hasKey(redisKey))) {
            return Result.error("验证码已发送，请查看邮箱或稍后再试");
        }

        // 3. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 4. 异步发送邮件
        emailEventPublisher.publish(new EmailSendEvent()
                .setTo(email).setCode(code).setType(EmailSendEvent.EmailType.REGISTER));

        // 5. 发送成功后存入 Redis
        redisUtils.set(redisKey, code, RedisConstants.EMAIL_CODE_EXPIRATION, TimeUnit.SECONDS);

        return Result.success("验证码已发送", null);
    }

    /**
     * 注册接口（邮箱验证码校验）
     * <p>
     * 注册流程：邮箱验证码校验 → 唯一性校验 → 通过 Feign 调用 mall-user 创建用户。
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody @Valid RegisterRequest request) {
        // 1. 邮箱验证码校验
        String redisKey = RedisConstants.EMAIL_REGISTER_CODE_PREFIX + request.getEmail();
        String storedCode = redisUtils.get(redisKey);

        if (!StringUtils.hasText(storedCode)) {
            throw new CaptchaInvalidException();
        }

        if (!storedCode.equals(request.getEmailCode())) {
            return Result.error("验证码错误");
        }

        redisUtils.delete(redisKey);

        // 2. 唯一性校验（通过 Feign 调用 mall-user）
        if (userServiceClient.checkUsername(request.getUsername())) {
            return Result.error("用户名已存在");
        }

        if (StringUtils.hasText(request.getPhone()) && userServiceClient.checkPhone(request.getPhone())) {
            return Result.error("该手机号已被注册");
        }

        if (userServiceClient.checkEmail(request.getEmail())) {
            return Result.error("该邮箱已被注册");
        }

        // 3. 通过 Feign 调用 mall-user 注册用户
        UserServiceClient.RegisterRequest registerReq = new UserServiceClient.RegisterRequest();
        registerReq.setUsername(request.getUsername());
        registerReq.setPassword(passwordEncoder.encode(request.getPassword()));
        registerReq.setEmail(request.getEmail());
        registerReq.setPhone(request.getPhone());
        registerReq.setRealName(request.getRealName());

        userServiceClient.register(registerReq);

        return Result.success("注册成功", null);
    }

    /**
     * 发送邮箱登录验证码
     */
    @PostMapping("/login/send-email-code")
    public Result<String> sendLoginEmailCode(@RequestBody @Valid SendEmailCodeRequest request) {
        // ========== 图形验证码校验（防批量调用） ==========
        String captchaKey = request.getCaptchaKey();
        String captcha = request.getCaptcha();
        String storedCaptcha = redisUtils.get(RedisConstants.CAPTCHA_PREFIX + captchaKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            throw new CaptchaInvalidException();
        }

        if (!storedCaptcha.equalsIgnoreCase(captcha)) {
            return Result.error("图形验证码错误");
        }

        redisUtils.delete(RedisConstants.CAPTCHA_PREFIX + captchaKey);
        // ========== 图形验证码校验结束 ==========

        // 1. 确认该邮箱已注册
        String email = request.getEmail();
        UserAuthDTO userAuth = userServiceClient.loadByEmail(email);
        if (userAuth == null) {
            return Result.error("该邮箱未注册");
        }

        // 2. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 3. 异步发送邮件
        String redisKey = RedisConstants.EMAIL_LOGIN_CODE_PREFIX + email;
        emailEventPublisher.publish(new EmailSendEvent()
                .setTo(email).setCode(code).setType(EmailSendEvent.EmailType.LOGIN));

        // 4. 发送成功后存入 Redis
        redisUtils.set(redisKey, code, RedisConstants.EMAIL_CODE_EXPIRATION, TimeUnit.SECONDS);

        return Result.success("验证码已发送", null);
    }

    /**
     * 用邮箱验证码登录（仅限普通用户）
     */
    @PostMapping("/login/email-code")
    public Result<Map<String, Object>> loginByEmailCode(@RequestBody @Valid EmailCodeLoginRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        // 1. 从 Redis 取出验证码并比对
        String redisKey = RedisConstants.EMAIL_LOGIN_CODE_PREFIX + email;
        String storedCode = redisUtils.get(redisKey);

        if (!StringUtils.hasText(storedCode)) {
            throw new CaptchaInvalidException();
        }

        if (!storedCode.equals(code)) {
            return Result.error("验证码错误");
        }

        redisUtils.delete(redisKey);

        // 2. 通过 Feign 查询用户
        UserAuthDTO userAuth = userServiceClient.loadByEmail(email);
        if (userAuth == null) {
            return Result.error("用户不存在");
        }

        // 校验用户状态
        if (userAuth.getStatus() == null || userAuth.getStatus() != 1) {
            return Result.error("账号已被禁用，无法登录");
        }

        // 3. 构建 User 对象（用于后续 Token 生成）
        User user = buildUserFromAuthDTO(userAuth);

        // 4. 角色校验：仅允许普通用户(USER)角色
        boolean hasUserRole = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> "USER".equals(role.getCode()));
        if (!hasUserRole) {
            return Result.error("该账号无普通用户权限，请使用管理员登录入口");
        }

        // 5. 生成 Token
        String token = IdUtil.fastSimpleUUID();
        String sessionId = IdUtil.fastSimpleUUID();

        redisUtils.set(
                RedisConstants.USER_CURRENT_SESSION_PREFIX + user.getId(),
                sessionId,
                RedisConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );
        redisUtils.set(
                RedisConstants.USER_ACTIVE_TOKEN_PREFIX + user.getId(),
                token,
                RedisConstants.TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sessionId", sessionId);
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("status", user.getStatus());

        Map<String, String> rolesMap = new HashMap<>();
        if (user.getRoles() != null) {
            for (var role : user.getRoles()) {
                rolesMap.put(role.getCode(), role.getName());
            }
        }
        userInfo.put("roles", rolesMap);

        redisUtils.setObject(
                RedisConstants.TOKEN_PREFIX + token,
                userInfo,
                RedisConstants.TOKEN_EXPIRATION,
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
     */
    @PostMapping("/reset-password/send-code")
    public Result<String> sendResetPasswordCode(@RequestBody @Valid SendEmailCodeRequest request) {
        // ========== 图形验证码校验（防批量调用） ==========
        String captchaKey = request.getCaptchaKey();
        String captcha = request.getCaptcha();
        String storedCaptcha = redisUtils.get(RedisConstants.CAPTCHA_PREFIX + captchaKey);

        if (!StringUtils.hasText(storedCaptcha)) {
            throw new CaptchaInvalidException();
        }

        if (!storedCaptcha.equalsIgnoreCase(captcha)) {
            return Result.error("图形验证码错误");
        }

        redisUtils.delete(RedisConstants.CAPTCHA_PREFIX + captchaKey);
        // ========== 图形验证码校验结束 ==========

        // 1. 确认该邮箱已注册
        String email = request.getEmail();
        UserAuthDTO userAuth = userServiceClient.loadByEmail(email);
        if (userAuth == null) {
            return Result.error("该邮箱未注册");
        }

        // 2. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 3. 异步发送邮件
        String redisKey = RedisConstants.EMAIL_RESET_PWD_CODE_PREFIX + email;
        emailEventPublisher.publish(new EmailSendEvent()
                .setTo(email).setCode(code).setType(EmailSendEvent.EmailType.RESET_PASSWORD));

        // 4. 发送成功后存入 Redis
        redisUtils.set(redisKey, code, RedisConstants.EMAIL_CODE_EXPIRATION, TimeUnit.SECONDS);

        return Result.success("验证码已发送", null);
    }

    /**
     * 重置密码
     * <p>
     * 校验邮箱验证码后，通过 Feign 调用 mall-user 更新密码。
     */
    @PostMapping("/reset-password/reset")
    public Result<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        // 1. 从 Redis 取出验证码并比对
        String redisKey = RedisConstants.EMAIL_RESET_PWD_CODE_PREFIX + email;
        String storedCode = redisUtils.get(redisKey);

        if (!StringUtils.hasText(storedCode)) {
            throw new CaptchaInvalidException();
        }

        if (!storedCode.equals(code)) {
            return Result.error("验证码错误");
        }

        redisUtils.delete(redisKey);

        // 2. 通过 Feign 查询用户
        UserAuthDTO userAuth = userServiceClient.loadByEmail(email);
        if (userAuth == null) {
            return Result.error("用户不存在");
        }

        // 3. 通过 Feign 更新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        userServiceClient.updatePassword(userAuth.getId(), encodedPassword);

        // 4. 清除该用户所有登录会话
        redisUtils.delete(RedisConstants.USER_CURRENT_SESSION_PREFIX + userAuth.getId());

        log.info("用户 {} 密码重置成功，已清除登录会话", userAuth.getUsername());
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

            Map<String, Object> userInfo = redisUtils.getObject(RedisConstants.TOKEN_PREFIX + token,
                    new TypeReference<Map<String, Object>>() {});
            if (userInfo != null) {
                Long userId = ((Number) userInfo.get("userId")).longValue();
                redisUtils.delete(RedisConstants.USER_CURRENT_SESSION_PREFIX + userId);
                redisUtils.delete(RedisConstants.USER_ACTIVE_TOKEN_PREFIX + userId);
            }

            redisUtils.delete(RedisConstants.TOKEN_PREFIX + token);
        }
        return Result.success("登出成功", null);
    }

    // ==================== 私有方法 ====================

    private void updateLastLogin(Long userId, String ip) {
        userServiceClient.updateLastLogin(userId, ip);
    }

    /**
     * 将 UserAuthDTO 转换为 mall-auth 的 User 实体
     */
    private User buildUserFromAuthDTO(UserAuthDTO userAuth) {
        User user = new User();
        user.setId(userAuth.getId());
        user.setUsername(userAuth.getUsername());
        user.setPassword(userAuth.getPassword());
        user.setEmail(userAuth.getEmail());
        user.setPhone(userAuth.getPhone());
        user.setAvatar(userAuth.getAvatar());
        user.setRealName(userAuth.getRealName());
        user.setStatus(userAuth.getStatus());

        if (userAuth.getRoles() != null) {
            List<Role> roles = userAuth.getRoles().stream()
                    .map(r -> {
                        Role role = new Role();
                        role.setId(r.getId());
                        role.setCode(r.getCode());
                        role.setName(r.getName());
                        return role;
                    })
                    .collect(Collectors.toList());
            user.setRoles(roles);

            List<GrantedAuthority> authorities = new ArrayList<>();
            for (RoleDTO role : userAuth.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            }
            user.setAuthorities(authorities);
        }

        return user;
    }

    // ==================== 内部请求类 ====================

    @Data
    public static class LoginRequest {
        @NotBlank(message = "账号不能为空")
        private String account;
        @Password
        private String password;
        @NotBlank(message = "验证码标识不能为空")
        private String captchaKey;
        @NotBlank(message = "验证码不能为空")
        private String captcha;
        private String ip;
    }

    @Data
    public static class RegisterRequest {
        @Username
        private String username;
        @Password
        private String password;
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "邮箱验证码不能为空")
        private String emailCode;
        @Phone
        private String phone;
        private String realName;
    }

    @Data
    public static class SendRegisterEmailCodeRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "验证码标识不能为空")
        private String captchaKey;
        @NotBlank(message = "验证码不能为空")
        private String captcha;
    }

    @Data
    public static class SendEmailCodeRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "验证码标识不能为空")
        private String captchaKey;
        @NotBlank(message = "验证码不能为空")
        private String captcha;
    }

    @Data
    public static class EmailCodeLoginRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "验证码不能为空")
        private String code;
        private String ip;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
        @NotBlank(message = "验证码不能为空")
        private String code;
        @Password
        private String newPassword;
    }
}