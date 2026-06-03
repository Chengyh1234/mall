package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.FileService;
import com.cyh.mallportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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

    /**
     * 获取当前用户信息
     *
     * @return 用户信息（不含密码）
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<User> getProfile() {
        Long userId = getCurrentUserId();
        User user = userService.getById(userId);
        if (user != null) {
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 更新用户基本信息（真实姓名、邮箱、手机号）
     * 更新邮箱或手机号时需要验证密码
     *
     * @param realName 真实姓名
     * @param email    邮箱（更新时需要密码验证）
     * @param phone    手机号（更新时需要密码验证）
     * @param password 当前密码（更新敏感信息时必填）
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<User> updateProfile(@RequestParam(required = false) String realName,
                                       @RequestParam(required = false) String email,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String password) {
        Long userId = getCurrentUserId();

        User user = userService.updateProfile(userId, realName, email, phone, password);
        return Result.success("更新成功", user);
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

        Map<String, String> uploadResult = fileService.uploadImage(file, "avatars");
        if (uploadResult == null) {
            return Result.error("头像上传失败");
        }

        String avatarUrl = uploadResult.get("relativePath");

        userService.updateAvatar(userId, avatarUrl);

        if (oldAvatar != null) {
            fileService.deleteFile(oldAvatar, "avatars");
        }

        Map<String, String> data = new HashMap<>();
        data.put("avatar", avatarUrl);
        data.put("avatarUrl", "/uploads/images/avatars/" + avatarUrl);
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