/*
 * Copyright 2026 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apzda.hajimi.captcha.provider;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.date.DateUtil;
import com.apzda.hajimi.captcha.Captcha;
import com.apzda.hajimi.captcha.ValidateStatus;
import com.apzda.hajimi.captcha.storage.CaptchaStorage;
import com.apzda.kalami.data.MapConfig;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 图片验证码.
 *
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
public class ImageCaptchaProvider implements CaptchaProvider<Map<String, Object>> {

    private CaptchaStorage captchaStorage;

    private MapConfig<Map<String, Object>> props;

    private CodeGenerator codeGenerator;

    @Override
    public void init(@NonNull CaptchaStorage storage, @NonNull Map<String, Object> config) throws Exception {
        this.captchaStorage = storage;
        this.props = new MapConfig<>(config);
        if (props.getBool("test-mode", false)) {
            this.codeGenerator = new CodeGenerator() {
                @Override
                public String generate() {
                    return "A12b";
                }

                @Override
                public boolean verify(String code, String userInputCode) {
                    return Objects.equals(code, userInputCode);
                }
            };
        }
        else if (StringUtils.isNotBlank(props.getString("codes"))) {
            this.codeGenerator = new RandomGenerator(props.getString("codes"), props.getInt("length", 4));
        }
        else {
            this.codeGenerator = new RandomGenerator(props.getInt("length", 4));
        }
    }

    @Override
    public String getId() {
        return "image";
    }

    @Override
    @Nonnull
    public Captcha create(String uuid, int width, int height, @Nonnull Duration duration) throws Exception {
        AbstractCaptcha captcha;
        val type = props.getString("type", "line");
        if ("line".equalsIgnoreCase(type)) {
            val lineCount = props.getInt("count", 60);
            captcha = CaptchaUtil.createLineCaptcha(width, height, codeGenerator, lineCount);
        }
        else if ("shear".equalsIgnoreCase(type)) {
            val thickness = props.getInt("count", 6);
            captcha = CaptchaUtil.createShearCaptcha(width, height, codeGenerator, thickness);
        }
        else {
            val circleCount = props.getInt("count", 8);
            captcha = CaptchaUtil.createCircleCaptcha(width, height, codeGenerator, circleCount);
        }
        captcha.setGenerator(codeGenerator);

        val code = captcha.getCode();
        val id = UUID.randomUUID().toString();
        val ca = new Captcha();
        ca.setId(id);
        ca.setCode(code);
        ca.setExpireTime(DateUtil.currentSeconds() + duration.toSeconds());
        captchaStorage.save(uuid, ca);
        val newCa = BeanUtil.copyProperties(ca, Captcha.class, "code");
        try (val b = new ByteArrayOutputStream()) {
            captcha.write(b);
            newCa.setCode("data:image/png;base64," + Base64Encoder.encode(b.toByteArray()));
        }

        return newCa;
    }

    @Override
    public ValidateStatus validate(String uuid, String id, String code, boolean removeOnInvalid) {
        val captcha = new Captcha();
        captcha.setId(id);
        val ca = captchaStorage.load(uuid, captcha);
        if (ca == null) {
            return ValidateStatus.EXPIRED;
        }
        val now = DateUtil.currentSeconds();
        if (now > ca.getExpireTime()) {
            captchaStorage.remove(uuid, captcha);
            return ValidateStatus.EXPIRED;
        }
        boolean correct;
        if (props.getBool("case-sensitive", false)) {
            correct = Objects.equals(code, ca.getCode());
        }
        else {
            correct = StringUtils.equalsIgnoreCase(code, ca.getCode());
        }
        if (!correct) {
            if (removeOnInvalid) {
                captchaStorage.remove(uuid, captcha);
            }
            return ValidateStatus.ERROR;
        }

        return ValidateStatus.OK;
    }

}
