package com.cyh.mallcommon.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 自定义密码校验注解
 * <p>
 * 规则：6-20位，不允许纯空格。
 *
 * @author cyh
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)//标明由哪个类执行校验逻辑
@Documented
public @interface Password {

    String message() default "密码长度必须为6-20位";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}