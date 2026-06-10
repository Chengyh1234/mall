package com.cyh.mallcommon.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 手机号校验器
 * <p>
 * 校验中国大陆手机号格式：1[3-9]开头，共11位数字。
 *
 * @author cyh
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {

    /** 中国大陆手机号正则 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 或空值不校验，由@NotBlank单独处理
        if (value == null || value.isEmpty()) {
            return true;
        }
        return PHONE_PATTERN.matcher(value).matches();
    }
}