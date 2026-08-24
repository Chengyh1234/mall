package com.cyh.mallauth.feign;

import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用户管理员统计 Feign 客户端
 * 调用 mall-user 获取平台用户统计数据
 */
@FeignClient(name = "mall-user", path = "/api/internal/user/admin")
public interface UserAdminCountClient {

    /**
     * 获取平台总用户数
     */
    @GetMapping("/user-count")
    Result<Long> getUserCount();
}