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
package com.apzda.kalami.common.openapi.autoconfig;

import com.apzda.kalami.common.openapi.ClientInfoProvider;
import com.apzda.kalami.common.openapi.config.OpenApiProperties;
import com.apzda.kalami.common.openapi.interceptor.OpenapiHandlerInterceptor;
import com.apzda.kalami.common.openapi.message.EncryptedMessageConverter;
import com.apzda.kalami.common.openapi.modem.ClientBasedAesModem;
import com.apzda.kalami.common.openapi.modem.ClientBasedRsaModem;
import com.apzda.kalami.common.openapi.modem.Modem;
import com.apzda.kalami.common.openapi.modem.PlainModem;
import com.apzda.kalami.common.openapi.sign.ClientBasedSha256Signer;
import com.apzda.kalami.common.openapi.sign.OpenApiSigner;
import com.apzda.kalami.common.openapi.token.TokenManager;
import com.apzda.kalami.i18n.I18n;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.util.Lazy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@AutoConfiguration(before = MessageSourceAutoConfiguration.class)
@ComponentScan("com.apzda.kalami.common.openapi.handler")
@EnableConfigurationProperties({MessageSourceProperties.class, OpenApiProperties.class})
public class OpenApiAutoConfiguration implements WebMvcConfigurer {
    private final Lazy<EncryptedMessageConverter> encryptedMessageConverterLazy;
    private final Lazy<OpenapiHandlerInterceptor> openapiHandlerInterceptor;

    OpenApiAutoConfiguration(ApplicationContext context) {
        encryptedMessageConverterLazy = Lazy.of(() -> context.getBean(EncryptedMessageConverter.class));
        openapiHandlerInterceptor = Lazy.of(() -> context.getBean(OpenapiHandlerInterceptor.class));
    }

    @Bean
    @ConditionalOnMissingBean
    static MessageSource messageSource(MessageSourceProperties properties) {
        if (!CollectionUtils.isEmpty(properties.getBasename())) {
            I18n.messageSource.addBasenames(properties.getBasename().toArray(new String[0]));
        }

        val cacheDuration = properties.getCacheDuration();

        if (cacheDuration != null) {
            I18n.messageSource.setCacheMillis(cacheDuration.toMillis());
        }

        log.info("MessageSource BaseNames: {}", I18n.messageSource.getBasenameSet());

        return I18n.messageSource;
    }

    @Bean
    @ConditionalOnMissingBean
    OpenApiSigner defaultOpenApiSigner(ClientInfoProvider provider) {
        return new ClientBasedSha256Signer(provider);
    }

    @Bean
    Modem defaultAesModem(ClientInfoProvider provider) {
        return new ClientBasedAesModem(provider);
    }

    @Bean
    Modem defaultRsaModem(ClientInfoProvider provider) {
        return new ClientBasedRsaModem(provider);
    }

    @Bean
    Modem defaultPlainModem() {
        return new PlainModem();
    }

    @Bean
    EncryptedMessageConverter encryptedMessageConverter(ObjectMapper objectMapper, ObjectProvider<Modem> modems, OpenApiSigner signer) {
        return new EncryptedMessageConverter(objectMapper, modems, signer);
    }

    @Bean
    HandlerInterceptor openApiHandlerInterceptor(ClientInfoProvider provider, TokenManager tokenManager, OpenApiProperties properties) {
        return new OpenapiHandlerInterceptor(provider, tokenManager, properties);
    }

    @Override
    public void addInterceptors(@Nonnull InterceptorRegistry registry) {
        registry.addInterceptor(openapiHandlerInterceptor.get());
    }

    @Override
    public void extendMessageConverters(@Nonnull List<HttpMessageConverter<?>> converters) {
        converters.add(0, this.encryptedMessageConverterLazy.get());
    }
}
