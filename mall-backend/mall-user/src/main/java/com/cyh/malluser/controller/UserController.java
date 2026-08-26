package com.cyh.malluser.controller;

import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallcommon.utils.Result;
import com.cyh.malluser.entity.User;
import com.cyh.malluser.mapper.RoleMapper;
import com.cyh.mallcommon.service.FileService;
import com.cyh.malluser.service.UserService;
import com.cyh.malluser.vo.UserInfoVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户信息管理控制器
 * <p>
 * 从网关注入的请求头 X-User-Id 获取当前用户ID。
 * 身份认证由网关统一处理（Token校验），SecurityContext 由
 * {@link com.cyh.malluser.filter.GatewayHeaderAuthenticationFilter} 构建。
 * 本控制器仅需登录即可访问（isAuthenticated），由网关保证用户已认证。
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;
    private final FileService fileService;
    private final RoleMapper roleMapper;

    /**
     * 获取当前用户信息（敏感字段已脱敏）
     */
    @GetMapping("/profile")
    public Result<UserInfoVo> getProfile(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            List<String> roles = roleMapper.selectByUserId(userId).stream()
                    .map(role -> role.getCode())
                    .toList();
            return Result.success(UserInfoVo.fromUser(user, roles));
        }
        return Result.error("用户不存在");
    }

    /**
     * 更新用户基本信息
     */
    @PutMapping("/profile")
    public Result<UserInfoVo> updateProfile(@RequestHeader("X-User-Id") Long userId,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) String realName,
                                            @RequestParam(required = false) String email,
                                            @RequestParam(required = false) String phone,
                                            @RequestParam(required = false) String password) {
        User user = userService.updateProfile(userId, username, realName, email, phone, password);
        List<String> roles = roleMapper.selectByUserId(userId).stream()
                .map(role -> role.getCode())
                .toList();
        UserInfoVo userInfoVo = UserInfoVo.fromUser(user, roles);
        return Result.success("更新成功", userInfoVo);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestHeader("X-User-Id") Long userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        boolean success = userService.updatePassword(userId, oldPassword, newPassword);
        if (success) {
            return Result.success("密码修改成功", null);
        }
        return Result.error("密码修改失败");
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestHeader("X-User-Id") Long userId,
                                                    @RequestParam("file") MultipartFile file) {
        String oldAvatar = userService.getById(userId).getAvatar();

        Map<String, String> uploadResult = fileService.uploadImage(file, FileConstants.USER_AVATARS);
        if (uploadResult == null) {
            return Result.error("头像上传失败");
        }

        String avatarUrl = uploadResult.get("relativePath");
        userService.updateAvatar(userId, avatarUrl);

        if (oldAvatar != null) {
            fileService.deleteFile(oldAvatar, FileConstants.USER_AVATARS);
        }

        Map<String, String> data = new HashMap<>();
        data.put("avatar", avatarUrl);
        data.put("avatarUrl", "/uploads/images/user/avatars/" + avatarUrl);
        return Result.success("头像更新成功", data);
    }
}