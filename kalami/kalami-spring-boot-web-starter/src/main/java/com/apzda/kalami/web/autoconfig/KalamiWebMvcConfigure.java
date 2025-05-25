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
package com.apzda.kalami.web.autoconfig;

import com.apzda.kalami.http.ExceptionTransformer;
import com.apzda.kalami.http.modem.Modem;
import com.apzda.kalami.infra.config.InfraConfigProperties;
import com.apzda.kalami.web.advice.KalamiControllerAdvice;
import com.apzda.kalami.web.converter.EncryptedMessageConverter;
import com.apzda.kalami.web.converter.modem.DefaultBase64EncodedModem;
import com.apzda.kalami.web.error.KalamiErrorController;
import com.apzda.kalami.web.error.KalamiErrorViewResolver;
import com.apzda.kalami.web.resolver.PagerResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Lazy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)

class KalamiWebMvcConfigure implements WebMvcConfigurer {

    private final Lazy<EncryptedMessageConverter> encryptedMessageConverter;

    private final Lazy<PagerResolver> pagerResolver;

    KalamiWebMvcConfigure(ApplicationContext context) {
        this.encryptedMessageConverter = Lazy.of(() -> context.getBean(EncryptedMessageConverter.class));
        this.pagerResolver = Lazy.of(() -> context.getBean(PagerResolver.class));
    }

    @Bean
    @ConditionalOnMissingBean
    static Modem modem(InfraConfigProperties properties) {
        return new DefaultBase64EncodedModem(properties.getModem());
    }

    @Bean
    static EncryptedMessageConverter encryptedMessageConverter(ObjectMapper objectMapper, Modem modem) {
        return new EncryptedMessageConverter(objectMapper, modem);
    }

    @Override
    public void configureMessageConverters(@Nonnull List<HttpMessageConverter<?>> converters) {
        converters.add(0, encryptedMessageConverter.get());
    }

    @Override
    public void addArgumentResolvers(@Nonnull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(0, pagerResolver.get());
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

    @Bean
    PagerResolver pageRequestResolver(InfraConfigProperties properties) {
        return new PagerResolver(properties);
    }

    @Bean
    KalamiControllerAdvice kalamiControllerAdvice(KalamiErrorController errorController,
            ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConverters,
            ObjectProvider<List<ExceptionTransformer>> transformers) {
        return new KalamiControllerAdvice(errorController, httpMessageConverters, transformers);
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
    KalamiErrorController basicErrorController(ErrorAttributes errorAttributes,
            ObjectProvider<ErrorViewResolver> errorViewResolvers, ServerProperties serverProperties) {
        return new KalamiErrorController(errorAttributes, serverProperties.getError(),
                errorViewResolvers.orderedStream().toList());
    }

    @Bean
    ErrorViewResolver kalamiErrorViewResolver(ApplicationContext applicationContext, WebProperties webProperties) {
        return new KalamiErrorViewResolver(applicationContext, webProperties.getResources());
    }

}
