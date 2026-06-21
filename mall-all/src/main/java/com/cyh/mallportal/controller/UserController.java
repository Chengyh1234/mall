package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallcommon.constant.FileConstants;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.mapper.RoleMapper;
import com.cyh.mallportal.mapper.UserMapper;
import com.cyh.mallportal.vo.UserInfoVo;
import com.cyh.mallportal.service.FileService;
import com.cyh.mallportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户信息管理控制器
 * 提供用户资料修改、密码修改、头像上传等功能
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前用户信息（敏感字段已脱敏）
     * 包含角色列表和权限列表
     *
     * @return 用户信息 VO（邮箱、手机号、真实姓名已脱敏）
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<UserInfoVo> getProfile() {
        Long userId = getCurrentUserId();
        User user = userService.getById(userId);
        if (user != null) {
            // 加载角色编码和权限编码
            List<String> roles = roleMapper.selectByUserId(userId).stream()
                    .map(role -> role.getCode())
                    .toList();
            return Result.success(UserInfoVo.fromUser(user, roles));
        }
        return Result.error("用户不存在");
    }

    /**
     * 更新用户基本信息（用户名、真实姓名、邮箱、手机号）
     * 更新用户名时校验唯一性；更新邮箱或手机号时需要验证密码
     *
     * @param username  用户名（更新时校验唯一性）
     * @param realName 真实姓名
     * @param email    邮箱（更新时需要密码验证）
     * @param phone    手机号（更新时需要密码验证）
     * @param password 当前密码（更新敏感信息时必填）
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<UserInfoVo> updateProfile(@RequestParam(required = false) String username,
                                       @RequestParam(required = false) String realName,
                                       @RequestParam(required = false) String email,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String password) {
        Long userId = getCurrentUserId();

        User user = userService.updateProfile(userId, username, realName, email, phone, password);
        // 加载角色编码和权限编码
        List<String> roles = roleMapper.selectByUserId(userId).stream()
                .map(role -> role.getCode())
                .toList();
        UserInfoVo userInfoVo = UserInfoVo.fromUser(user, roles);
        return Result.success("更新成功", userInfoVo);
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updatePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        Long userId = getCurrentUserId();

        // 参数校验
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
     *
     * @param file 头像图片文件
     * @return 头像URL
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();

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

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        }
        return null;
    }
}