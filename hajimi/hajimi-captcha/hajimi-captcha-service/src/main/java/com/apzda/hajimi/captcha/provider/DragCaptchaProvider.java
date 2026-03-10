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

import cn.hutool.core.date.DateUtil;
import com.apzda.hajimi.captcha.Captcha;
import com.apzda.hajimi.captcha.ValidateStatus;
import com.apzda.hajimi.captcha.storage.CaptchaStorage;
import com.apzda.kalami.data.MapConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.util.Lazy;

import java.time.Duration;
import java.util.*;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
public class DragCaptchaProvider implements CaptchaProvider<Map<String, Object>>, ApplicationContextAware {

    private CaptchaStorage captchaStorage;

    private MapConfig<Map<String, Object>> props;

    private double maxMultiple = 1.15d;

    private Lazy<ObjectMapper> objectMapperLoader;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.objectMapperLoader = Lazy.of(() -> applicationContext.getBean(ObjectMapper.class));
    }

    @Override
    public void init(@Nonnull CaptchaStorage storage, @Nonnull Map<String, Object> config) throws Exception {
        this.captchaStorage = storage;
        this.props = new MapConfig<>(config);
        maxMultiple = props.getDouble("max-multiple", maxMultiple);
        if (maxMultiple < 1.05d) {
            maxMultiple = 1.05d;
        }
        else if (maxMultiple > 2d) {
            maxMultiple = 2d;
        }
    }

    @Override
    public String getId() {
        return "drag";
    }

    @Override
    @Nonnull
    public Captcha create(String uuid, int width, int height, @Nonnull Duration duration) throws Exception {
        val id = UUID.randomUUID().toString();
        val ca = new Captcha();
        ca.setId(id);
        var code = new Random().nextDouble(1.05d, maxMultiple);
        ca.setCode(String.valueOf(code));
        ca.setExpireTime(DateUtil.currentSeconds() + duration.toSeconds());
        captchaStorage.save(uuid, ca);
        return ca;
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

        if (!verify(code, ca.getCode())) {
            if (removeOnInvalid) {
                captchaStorage.remove(uuid, captcha);
            }
            return ValidateStatus.ERROR;
        }
        return ValidateStatus.OK;
    }

    private boolean verify(String encoded, String code) {
        try {
            val objectMapper = objectMapperLoader.get();
            val dto = objectMapper.readValue(Base64.getDecoder().decode(encoded), DragDTO.class);
            if (dto.beginTime == null) {
                return false;
            }
            if (dto.endTime == null) {
                return false;
            }
            val time = dto.endTime - dto.beginTime;
            if (time < props.getLong("min-time", 350) || time > props.getLong("max-time", 3500)) {
                return false;
            }
            if (dto.points == null) {
                return false;
            }
            val size = dto.points.size();
            if (size < props.getLong("min-points", 15) || size > props.getLong("max-points", 1000)) {
                return false;
            }

            val p0 = dto.points.get(0);
            val p1 = dto.points.get(size - 1);
            val d = Math.abs(p1.get(1) - p0.get(1));

            if (d < props.getLong("min-diff", 3)) {
                return false;
            }
            val dCode = Double.parseDouble(code);
            val width = (p1.get(0) - p0.get(0)) / dCode;
            if (width < dto.width) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }

    @Data
    public static class DragDTO {

        private Long beginTime;

        private Long endTime;

        private Double width;

        private List<List<Double>> points;

    }

}
