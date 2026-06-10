package com.cyh.mallcommon.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 用户名校验器
 * <p>
 * 校验用户名格式：4-20位，仅允许字母和数字，必须同时包含至少一个字母和一个数字。
 *
 * @author cyh
 */
public class UsernameValidator implements ConstraintValidator<Username, String> {

    /** 正则：必须同时包含字母和数字，且仅允许字母和数字 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z0-9]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null值不校验，由@NotBlank单独处理
        if (value == null || value.isEmpty()) {
            return true;
        }
        // 长度校验 + 正则校验
        return value.length() >= 4 && value.length() <= 20 && USERNAME_PATTERN.matcher(value).matches();
    }
}