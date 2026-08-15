package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理 VO
 *
 * 管理员分页查看用户列表专用，排除 password/lastLoginIp/updatedAt/isDeleted 等内部字段
 * 对应接口：
 *   GET /user/manage/page
 */
@Data
public class UserManageVo {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 状态: 1-启用 0-禁用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 角色编码列表 */
    private List<String> roles;

    public static UserManageVo fromUser(User user, List<String> roles) {
        if (user == null) {
            return null;
        }
        UserManageVo vo = new UserManageVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setRoles(roles);
        return vo;
    }
}