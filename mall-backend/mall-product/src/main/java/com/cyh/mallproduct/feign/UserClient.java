package com.cyh.mallproduct.feign;

import com.cyh.mallcommon.dto.UserProfileVo;
import com.cyh.mallcommon.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端
 * <p>
 * 调用 mall-user 的内部接口获取用户基本信息，替代直连数据库。
 */
@FeignClient(name = "mall-user", path = "/api")
public interface UserClient {

    /**
     * 根据用户 ID 获取用户基本信息
     */
    @GetMapping("/internal/user/detail/{id}")
    Result<UserProfileVo> getUserDetail(@PathVariable("id") Long id);
}