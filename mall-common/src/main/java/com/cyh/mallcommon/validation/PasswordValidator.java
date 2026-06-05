package com.cyh.mallcommon.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 密码校验器
 * <p>
 * 校验密码格式：6-20位，不包含空格。
 * ConstraintValidator<Password, String>String是校验的字段类型
 * @author cyh
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null值不校验，由@NotBlank单独处理
        if (value == null || value.isEmpty()) {
            return true;
        }
        // 6-20位，且不能包含空格
        return value.length() >= 6 && value.length() <= 20 && !value.contains(" ");
    }
}