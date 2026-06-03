package com.cyh.mallportal.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallcommon.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 * <p>
 * 提供图形验证码的生成和获取接口，用于登录时的验证码验证。
 * 使用 Hutool 生成图形验证码，验证码文本存储在 Redis 中，5分钟过期。
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取图形验证码
     * <p>
     * 生成一个4位数字的图形验证码，返回 Base64 格式的图片和唯一标识 key。
     * 前端在登录时需要提交该 captchaKey 和用户输入的验证码内容。
     *
     * @return 包含 captchaKey 和 captchaImage 的响应结果
     */
    @GetMapping
    public Result<Map<String, String>> getCaptcha() {
        // 1. 使用 Hutool 生成图形验证码：4位数字，宽120px，高40px，10个干扰圆圈
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(120, 40, 4, 10);

        // 2. 生成唯一 Key 作为 Redis 存储标识
        String captchaKey = IdUtil.fastSimpleUUID();

        // 3. 将验证码文本存入 Redis（5分钟过期，一次性使用）
        redisTemplate.opsForValue().set(
                MyConstants.CAPTCHA_PREFIX + captchaKey,
                captcha.getCode(),
                MyConstants.CAPTCHA_EXPIRATION,
                TimeUnit.SECONDS
        );

        log.debug("生成验证码: key={}, code={}", captchaKey, captcha.getCode());

        // 4. 将图片转为 Base64 格式返回
        String base64Image = captcha.getImageBase64();

        Map<String, String> data = new HashMap<>();
        data.put("captchaKey", captchaKey);
        data.put("captchaImage", "data:image/png;base64," + base64Image);

        return Result.success(data);
    }
}