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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.EnumerationIter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.apzda.hajimi.captcha.Captcha;
import com.apzda.hajimi.captcha.SerializableStream;
import com.apzda.hajimi.captcha.ValidateStatus;
import com.apzda.hajimi.captcha.storage.CaptchaStorage;
import com.apzda.hajimi.captcha.utils.SliderUtil;
import com.apzda.kalami.data.MapConfig;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * 滑动验证码.
 *
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
public class SliderCaptchaProvider implements CaptchaProvider<Map<String, Object>> {

    private static final Pattern JAR_FILE_PATTERN = Pattern.compile("^file:(.+?\\.jar)!/(.+)$");

    private int imagesCnt;

    private List<File> sliders;

    private int slidersCnt;

    private LoadingCache<Integer, SerializableStream> imagesCache;

    private LoadingCache<Integer, SerializableStream> sliderCache;

    private CaptchaStorage captchaStorage;

    private String watermark;

    private int noise;

    private int tolerant;

    private String path;

    private boolean testMode;

    @Override
    public void init(@Nonnull CaptchaStorage storage, @Nonnull Map<String, Object> config) throws Exception {
        this.captchaStorage = storage;
        MapConfig<Map<String, Object>> props = new MapConfig<>(config);
        this.watermark = props.getString("watermark", "");
        this.noise = props.getInt("noise", 1);
        this.tolerant = props.getInt("tolerant", 5);
        this.path = StringUtils.appendIfMissing(props.getString("path", "slider/"), "/");
        this.testMode = props.getBool("test-mode", false);

        List<File> images = loadImages(false);
        imagesCnt = images.size();

        sliders = loadImages(true);
        slidersCnt = sliders.size();

        if (imagesCnt == 0 || slidersCnt == 0) {
            throw new IllegalStateException("images(" + imagesCnt + ") or slider(" + slidersCnt + ") is empty");
        }

        imagesCache = CacheBuilder.newBuilder().build(new ImageCacheLoader(images));
        sliderCache = CacheBuilder.newBuilder().build(new ImageCacheLoader(sliders));

        log.info("SliderCaptchaProvider initialized: images({}), slider({}), watermark({}), noise({}), tolerant({})",
                imagesCnt, slidersCnt, watermark, noise, tolerant);
    }

    @Override
    public String getId() {
        return "slider";
    }

    @Override
    @Nonnull
    public Captcha create(String uuid, int width, int height, @Nonnull Duration duration) throws Exception {
        val random = new Random();
        val imgIdx = random.nextInt(imagesCnt);
        val sliderIdx = random.nextInt(slidersCnt);

        val originalFile = imagesCache.get(imgIdx);
        val sliderFile = sliderCache.get(sliderIdx);
        val noiseFile = sliderCache.get(sliderIdx == sliders.size() - 1 ? sliderIdx - 1 : sliderIdx + 1);

        val sliderCaptcha = SliderUtil.createSliderCaptcha(sliderFile, noiseFile, originalFile, watermark, noise);
        val ca = new Captcha();
        val code = sliderCaptcha.getRx() + "";
        val id = UUID.randomUUID().toString();
        ca.setId(id);
        ca.setCode(code);
        ca.setExpireTime(DateUtil.currentSeconds() + duration.toSeconds());
        captchaStorage.save(uuid, ca);
        val captcha = BeanUtil.copyProperties(ca, Captcha.class, "code");
        captcha
            .setCode(sliderCaptcha.getBackImg() + "&&" + sliderCaptcha.getSliderImg() + "&&" + sliderCaptcha.getRy());
        return captcha;
    }

    @Override
    public ValidateStatus validate(String uuid, String id, String code, boolean removeOnInvalid) {
        log.debug("Validate Captcha(uuid:{}, id:{}, code: {})", uuid, id, code);
        val xy = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(code);
        if (xy.size() != 2) {
            return ValidateStatus.ERROR;
        }
        val captcha = new Captcha();
        captcha.setId(id);
        val ca = captchaStorage.load(uuid, captcha);
        if (ca == null) {
            log.debug("Cannot load captcha(uuid: {}, id:{})", uuid, id);
            return ValidateStatus.EXPIRED;
        }
        log.debug("Captcha(uuid: {}, id:{}, code: {}) loaded", uuid, id, ca.getCode());
        val now = DateUtil.currentSeconds();
        if (now > ca.getExpireTime()) {
            captchaStorage.remove(uuid, captcha);
            return ValidateStatus.EXPIRED;
        }
        val xPos = Integer.parseInt(ca.getCode());
        val randomX = Integer.parseInt(xy.get(0));
        if (testMode || Math.abs(randomX - xPos) <= tolerant) {
            return ValidateStatus.OK;
        }
        if (removeOnInvalid) {
            captchaStorage.remove(uuid, captcha);
        }
        return ValidateStatus.ERROR;
    }

    @Nonnull
    private List<File> loadImages(boolean slider) {
        EnumerationIter<URL> images;

        if (slider) {
            val resource = getClass().getClassLoader().getResource(this.path + "sliders");
            if (resource != null) {
                val files = load(resource);
                if (!files.isEmpty()) {
                    return files;
                }
            }
            images = ResourceUtil.getResourceIter(this.path + "sliders", getClass().getClassLoader());
        }
        else {
            val resource = getClass().getClassLoader().getResource(this.path + "images");
            if (resource != null) {
                val files = load(resource);
                if (!files.isEmpty()) {
                    return files;
                }
            }
            images = ResourceUtil.getResourceIter(this.path + "images", getClass().getClassLoader());
        }

        List<File> image = new ArrayList<>();
        images.forEach(url -> {
            try {
                val file = ResourceUtils.getFile(url);
                if (file.isDirectory()) {
                    val files = file.listFiles(File::isFile);
                    if (files != null) {
                        image.addAll(Arrays.asList(files));
                    }
                }
                else {
                    image.add(file);
                }
            }
            catch (FileNotFoundException ignored) {
            }
        });

        return image;
    }

    @Nonnull
    private List<File> load(@Nonnull URL resource) {
        List<File> images = new ArrayList<>();
        val matcher = JAR_FILE_PATTERN.matcher(resource.getPath());
        if (matcher.matches()) {
            val prefix = StringUtils.appendIfMissing(matcher.group(2), "/");
            try (val jarFile = new JarFile(matcher.group(1))) {
                jarFile.entries().asIterator().forEachRemaining(jarEntry -> {
                    if (!jarEntry.isDirectory() && jarEntry.getName().startsWith(prefix)) {
                        val tmpFile = FileUtil.createTempFile();
                        try (val to = new FileOutputStream(tmpFile)) {
                            StreamUtils.copy(ResourceUtil.getStream(jarEntry.getName()), new FileOutputStream(tmpFile));
                            images.add(tmpFile);
                        }
                        catch (Exception e) {
                            log.warn("Error loading image {}", jarEntry.getName());
                        }
                    }
                });
            }
            catch (Exception e) {
                log.warn("Failed to load images from {}", resource.getPath(), e);
            }
        }
        else {
            try {
                val file = ResourceUtils.getFile(resource);
                if (file.isDirectory()) {
                    val files = file.listFiles(File::isFile);
                    if (files != null) {
                        images.addAll(Arrays.asList(files));
                    }
                }
            }
            catch (Exception e) {
                log.warn("Failed to load images from {}", resource.getPath(), e);
            }
        }

        return images;
    }

}
