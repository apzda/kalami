/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.http;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Base64;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
public class Base64DecodeMultipartFile implements MultipartFile {

    private final byte[] imgContent;

    private final String header;

    public Base64DecodeMultipartFile(byte[] imgContent, @Nonnull String header) {
        this.imgContent = imgContent;
        this.header = header.split(";")[0];
    }

    @Override
    @Nonnull
    public String getName() {
        return System.currentTimeMillis() + Math.random() + "." + header.split("/")[1];
    }

    @Override
    @Nonnull
    public String getOriginalFilename() {
        return System.currentTimeMillis() + (int) (Math.random() * 10000) + "." + header.split("/")[1];
    }

    @Override
    public String getContentType() {
        return header.split(":")[1];
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @Override
    @Nonnull
    public byte[] getBytes() throws IOException {
        return imgContent;
    }

    @Override
    @Nonnull
    public InputStream getInputStream() {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException {
        try (val stream = new FileOutputStream(dest)) {
            stream.write(imgContent);
        }
    }

    @Nonnull
    public static MultipartFile base64Convert(@Nonnull String base64) {
        val base = base64.split(",");
        val decoder = Base64.getDecoder();
        byte[] b = decoder.decode(base[1]);

        for (int i = 0; i < b.length; ++i) {
            if (b[i] < 0) {
                b[i] += (byte) 256;
            }
        }
        return new Base64DecodeMultipartFile(b, base[0]);
    }

    @Nullable
    public static InputStream base64ToInputStream(String base64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new ByteArrayInputStream(bytes);
        }
        catch (Exception e) {
            log.error("base64 String to InputStream Error: {}", e.getMessage());
        }
        return null;
    }

    @NonNull
    public static String inputStreamToStream(InputStream in) throws IOException {
        byte[] data;
        try (val swapStream = new ByteArrayOutputStream(); val stream = new BufferedInputStream(in)) {
            byte[] buff = new byte[1024];
            int rc = 0;
            while ((rc = stream.read(buff, 0, 1024)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            data = swapStream.toByteArray();
        }
        return Base64.getEncoder().encodeToString(data);
    }

}
