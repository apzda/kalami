/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "kalami.config")
public class InfraConfigProperties {

    private static final String DEFAULT_PAGE_PARAMETER = "pageNumber";

    private static final String DEFAULT_SIZE_PARAMETER = "pageSize";

    private static final String DEFAULT_SORT_PARAMETER = "pageSorts";

    @DurationUnit(ChronoUnit.HOURS)
    private Duration tempExpireTime = Duration.ofHours(168);

    private final ModemConfig modem = new ModemConfig();

    private String pageParameter = DEFAULT_PAGE_PARAMETER;

    private String sizeParameter = DEFAULT_SIZE_PARAMETER;

    private String sortParameter = DEFAULT_SORT_PARAMETER;

    @Data
    public static final class ModemConfig {

        private Algorithm algorithm = Algorithm.AES;

        private String mode = "CBC";

        private String padding = "PKCS5Padding";

        private String iv = "0102030405060708";

        private String key = "0CoJUm6Qyw8W8jud";

    }

    public enum Algorithm {

        AES, DES

    }

}
