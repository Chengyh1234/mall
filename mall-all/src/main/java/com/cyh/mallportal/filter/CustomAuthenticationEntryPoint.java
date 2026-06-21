package com.cyh.mallportal.filter;

import cn.hutool.json.JSONUtil;
import com.cyh.mallcommon.exception.ErrorCode;
import com.cyh.mallcommon.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义未认证入口点
 * <p>
 * 当未登录的用户访问受保护接口时，Spring Security 的 ExceptionTranslationFilter会调用此处理器，返回统一的 JSON 响应。
 * ExceptionTranslationFilter在security中专门捕获过滤器链抛出的异常，处理在OncePerRequestFilter之后
 * <p>
 * HTTP 状态码：401 | 业务码：40102（通用未登录，非具体认证失败）
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(ErrorCode.NOT_LOGGED_IN.getHttpStatus());
        Result<Object> error = Result.error(ErrorCode.NOT_LOGGED_IN.getBusinessCode(), ErrorCode.NOT_LOGGED_IN.getMessage());
        response.getWriter().write(JSONUtil.toJsonStr(error));
    }
}