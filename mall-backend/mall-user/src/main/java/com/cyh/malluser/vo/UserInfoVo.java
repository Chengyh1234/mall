package com.cyh.malluser.vo;

import com.cyh.malluser.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息 VO
 */
@Data
public class UserInfoVo {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String realName;
    private LocalDateTime createdAt;
    private List<String> roles;

    public static UserInfoVo fromUser(User user, List<String> roles) {
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
        return vo;
    }

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

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

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