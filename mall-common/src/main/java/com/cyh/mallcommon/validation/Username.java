package com.cyh.mallcommon.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 自定义用户名校验注解
 * <p>
 * 规则：4-20位，仅允许字母和数字，必须同时包含至少一个字母和一个数字。
 * 防止用户名与手机号（纯数字）或邮箱（含@）混淆，保证统一账号登录的准确性。
 *
 * @author cyh
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)//标明由哪个类执行校验逻辑
@Documented
public @interface Username {

    String message() default "用户名必须为4-20位，且包含字母和数字，不能包含特殊符号";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}