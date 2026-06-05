package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息 VO
 * 返回给前端的用户信息，对敏感字段进行脱敏处理
 */
@Data
public class UserInfoVo {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱（脱敏，如 ****@example.com） */
    private String email;

    /** 手机号（脱敏，如 138****0000） */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 真实姓名（脱敏，如 程**） */
    private String realName;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 角色编码列表（如 ["USER", "SELLER"]） */
    private List<String> roles;

    /** 权限编码列表（如 ["ROLE_USER", "product:add", ...]） */
    private List<String> authorities;

    /**
     * 从 User 实体创建 UserInfoVo，并自动脱敏处理
     *
     * @param user       用户实体
     * @param roles      角色编码列表（如 ["USER", "SELLER"]）
     * @param authorities 权限编码列表（如 ["ROLE_USER", "product:add"]）
     * @return UserInfoVo
     */
    public static UserInfoVo fromUser(User user, List<String> roles, List<String> authorities) {
        if (user == null) {
            return null;
        }
        UserInfoVo vo = new UserInfoVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(maskEmail(user.getEmail()));
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setAvatar(user.getAvatar());
        vo.setRealName(maskRealName(user.getRealName()));
        vo.setCreatedAt(user.getCreatedAt());
        vo.setRoles(roles);
        vo.setAuthorities(authorities);
        return vo;
    }

    /**
     * 邮箱脱敏：将 @ 之前的内容替换为 ****
     * 例如：abc@example.com → ****@example.com
     */
    private static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        return "****" + email.substring(atIndex);
    }

    /**
     * 手机号脱敏：保留前3位和后4位，中间替换为 ****
     * 例如：13812345678 → 138****5678
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 真实姓名脱敏：保留姓（第一个字符），其余替换为 *
     * 例如：程咬金 → 程**、张三 → 张*
     */
    private static String maskRealName(String realName) {
        if (realName == null || realName.isEmpty()) {
            return realName;
        }
        if (realName.length() == 1) {
            return realName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(realName.charAt(0));
        for (int i = 1; i < realName.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}