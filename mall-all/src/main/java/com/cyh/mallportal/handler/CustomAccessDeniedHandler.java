package com.cyh.mallportal.handler;

import cn.hutool.json.JSONUtil;
import com.cyh.mallcommon.exception.ErrorCode;
import com.cyh.mallcommon.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义权限不足处理器
 * 是对在.requestMatchers("/admin/**").hasRole("ADMIN")中权限捕不足进行捕捉，发生在security过滤链中ExceptionTranslationFilter
 * <p>
 * 当已登录但权限不足的用户访问接口时，Spring Security 的 ExceptionTranslationFilter
 * 会调用此处理器，返回统一的 JSON 响应。
 * <p>
 * HTTP 状态码：403 | 业务码：403
 */
//@Component
//public class CustomAccessDeniedHandler implements AccessDeniedHandler {
//
//    @Override
//    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
//        response.setContentType("application/json;charset=UTF-8");
//        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus());
//        Result<Object> error = Result.error(ErrorCode.FORBIDDEN.getBusinessCode(), ErrorCode.FORBIDDEN.getMessage());
//        response.getWriter().write(JSONUtil.toJsonStr(error));
//    }
//}