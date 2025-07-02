/*
 * Copyright 2025 the original author or authors.
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
package com.apzda.kalami.autoconfig;

import com.apzda.kalami.properties.OkHttpClientProperties;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author john <luxi520cn@163.com>
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(OkHttpClientProperties.class)
@RequiredArgsConstructor
public class OkHttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OkHttpClient.class)
    OkHttpClient okHttpClient(OkHttpClientProperties okHttpClientProperties) {
        return createOkHttpClient(okHttpClientProperties);
    }

    @Nonnull
    private OkHttpClient createOkHttpClient(@Nonnull OkHttpClientProperties okHttpClientProperties) {
        try {
            X509TrustManager x509TrustManager = x509TrustManager();
            SSLSocketFactory sslSocketFactory = sslSocketFactory(x509TrustManager);

            return okHttpClient(sslSocketFactory, x509TrustManager, okHttpClientProperties);
        }
        catch (Exception e) {
            String msg = "OkHttpClient-启动失败, 请检查OkHttp的配置";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Nonnull
    private X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private SSLSocketFactory sslSocketFactory(X509TrustManager x509TrustManager) {
        try {
            // 信任任何链接
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { x509TrustManager }, new SecureRandom());
            return sslContext.getSocketFactory();
        }
        catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("SSLSocketFactory-异常", e);
        }
    }

    @Nonnull
    private OkHttpClient okHttpClient(@Nonnull SSLSocketFactory sslSocketFactory,
            @Nonnull X509TrustManager x509TrustManager, @Nonnull OkHttpClientProperties okHttpClientProperties) {
        return new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, x509TrustManager)
            .retryOnConnectionFailure(true)
            .connectionPool(new ConnectionPool(okHttpClientProperties.getMaxIdleConnections(),
                    okHttpClientProperties.getKeepAliveDuration(), TimeUnit.MILLISECONDS))
            .connectTimeout(okHttpClientProperties.getConnectTimeOut(), TimeUnit.MILLISECONDS)
            .readTimeout(okHttpClientProperties.getReadTimeOut(), TimeUnit.MILLISECONDS)
            .writeTimeout(okHttpClientProperties.getWriteTimeOut(), TimeUnit.MILLISECONDS)
            .build();
    }

}
