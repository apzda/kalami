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
package com.apzda.kalami.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * OkHttp
 *
 * @author john <luxi520cn@163.com>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpTool {

    private static final String MSG_01 = "HTTP响应异常, status code ";

    private static final String MSG_02 = "HTTP响应异常, 耗时(ms)=";

    private static final OkHttpClient OK_HTTP_CLIENT = defaultOkHttpClient();

    public static OkHttpClient defaultOkHttpClient() {
        return ComTool.getBean(OkHttpClient.class);
    }

    /**
     * POST
     * @param url 请求地址
     * @param param 请求参数
     */
    public static String post(String url, Map<String, Object> param) {
        return post(url, param, null);
    }

    /**
     * GET
     * @param url 请求地址
     * @param param 请求参数
     */
    public static String get(String url, Map<String, Object> param) {
        return get(url, param, null);
    }

    /**
     * POST-JSON
     * @param url 请求地址
     * @param jsonString JSON字符串
     */
    public static String postJson(String url, String jsonString) {
        return post(url, jsonString, org.springframework.http.MediaType.APPLICATION_JSON);
    }

    /**
     * POST-JSON
     * @param url 请求地址
     * @param jsonString JSON字符串
     * @param header 请求头
     */
    public static String postJson(String url, String jsonString, Map<String, Object> header) {
        return post(url, jsonString, header, org.springframework.http.MediaType.APPLICATION_JSON);
    }

    /**
     * PUT-JSON
     * @param url 请求地址
     * @param jsonString JSON字符串
     * @param header 请求头
     */
    public static String putJson(String url, String jsonString, Map<String, Object> header) {
        return put(url, jsonString, header, org.springframework.http.MediaType.APPLICATION_JSON);
    }

    /**
     * POST-XML
     * @param url 请求地址
     * @param xmlString XML字符串
     */
    public static String postXml(String url, String xmlString) {
        return post(url, xmlString, org.springframework.http.MediaType.APPLICATION_XML);
    }

    /**
     * POST-XML
     * @param url 请求地址
     * @param xmlString XML字符串
     * @param header 请求头
     */
    public static String postXml(String url, String xmlString, Map<String, Object> header) {
        return post(url, xmlString, header, org.springframework.http.MediaType.APPLICATION_XML);
    }

    /**
     * GET
     * @param url 请求地址
     * @param param 请求参数
     * @param header 请求头
     */
    public static String get(String url, Map<String, Object> param, Map<String, Object> header) {
        LocalDateTime st = LocalDateTime.now();
        try {
            StringBuilder sb = new StringBuilder(url);
            if (param != null) {
                boolean firstFlag = true;
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    if (firstFlag) {
                        sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                        firstFlag = false;
                    }
                    else {
                        sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                    }
                }
            }
            Request.Builder builder = new Request.Builder();
            if (header != null) {
                for (Map.Entry<String, Object> entry : header.entrySet()) {
                    builder = builder.addHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            Request request = builder.url(sb.toString()).build();
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                return response.body().string();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(MSG_02 + Duration.between(st, LocalDateTime.now()).toMillis());
        }
    }

    /**
     * POST
     * @param url 请求地址
     * @param param 请求参数
     * @param header 请求头
     */
    public static String post(String url, Map<String, Object> param, Map<String, Object> header) {
        LocalDateTime st = LocalDateTime.now();
        try {
            // 请求参数
            FormBody.Builder formBuilder = new FormBody.Builder();
            if (param != null) {
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    formBuilder.add(entry.getKey(), entry.getValue().toString());
                }
            }

            // 请求头
            Request.Builder builder = new Request.Builder().url(url).post(formBuilder.build());
            if (header != null) {
                for (Map.Entry<String, Object> entry : header.entrySet()) {
                    builder = builder.addHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            Request request = builder.build();
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                return response.body().string();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(MSG_02 + Duration.between(st, LocalDateTime.now()).toMillis());
        }
    }

    /**
     * POST
     * @param url 请求地址
     * @param content 请求内容
     * @param mediaType 媒体类型
     */
    public static String post(String url, String content, org.springframework.http.MediaType mediaType) {
        LocalDateTime st = LocalDateTime.now();

        try {
            Request request = new Request.Builder().url(url)
                .post(RequestBody.Companion.create(content != null ? content : "",
                        MediaType.parse(mediaType.toString())))
                .build();
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                return response.body().string();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(MSG_02 + Duration.between(st, LocalDateTime.now()).toMillis());
        }
    }

    /**
     * POST
     * @param url 请求地址
     * @param content 请求内容
     * @param header 请求头
     * @param mediaType 媒体类型
     */
    public static String post(String url, String content, Map<String, Object> header,
            org.springframework.http.MediaType mediaType) {
        LocalDateTime st = LocalDateTime.now();
        try {
            Request.Builder builder = new Request.Builder().url(url)
                .post(RequestBody.Companion.create(content != null ? content : "",
                        MediaType.parse(mediaType.toString())));
            if (header != null) {
                for (Map.Entry<String, Object> entry : header.entrySet()) {
                    builder = builder.addHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            Request request = builder.build();
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                return response.body().string();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(MSG_02 + Duration.between(st, LocalDateTime.now()).toMillis());
        }
    }

    /**
     * 获取网络文件
     * @param urlStr 网络文件地址
     */
    public static byte[] getUrlFileByteArray(String urlStr) {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            URL url = new URI(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3 * 1000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            in = conn.getInputStream();
            out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return out.toByteArray();
        }
        catch (IOException e) {
            log.error("获取Url文件异常", e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    log.error("流关闭异常", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    log.error("流关闭异常", e);
                }
            }
        }
        return null;
    }

    /**
     * PUT
     * @param url 请求地址
     * @param content 请求内容
     * @param header 请求头
     * @param mediaType 媒体类型
     */
    public static String put(String url, String content, Map<String, Object> header,
            org.springframework.http.MediaType mediaType) {
        LocalDateTime st = LocalDateTime.now();
        try {
            Request.Builder builder = new Request.Builder().url(url)
                .put(RequestBody.Companion.create(content != null ? content : "",
                        MediaType.parse(mediaType.toString())));
            if (header != null) {
                for (Map.Entry<String, Object> entry : header.entrySet()) {
                    builder = builder.addHeader(entry.getKey(), entry.getValue().toString());
                }
            }
            Request request = builder.build();
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return Objects.requireNonNull(response.body()).string();
                }
                throw new RuntimeException(MSG_01 + response.code());
            }
        }
        catch (IOException e) {
            throw new RuntimeException(MSG_02 + Duration.between(st, LocalDateTime.now()).toMillis());
        }
    }

}
