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

package com.apzda.kalami.security.token;

import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
public class JWTSignerAdapter implements JWTSigner, InitializingBean {

    private final static ThreadLocal<JWTSigner> jwtSigners = new ThreadLocal<>();

    private final SecurityConfigProperties properties;

    public JWTSignerAdapter(SecurityConfigProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasText(properties.getJwtKey(), "property 'kalami.security.jwt-key' is blank");
    }

    @Override
    public String sign(String headerBase64, String payloadBase64) {
        return getJwtSigner().sign(headerBase64, payloadBase64);
    }

    @Override
    public boolean verify(String headerBase64, String payloadBase64, String signBase64) {
        return getJwtSigner().verify(headerBase64, payloadBase64, signBase64);
    }

    @Override
    public String getAlgorithm() {
        return getJwtSigner().getAlgorithm();
    }

    JWTSigner getJwtSigner() {
        var signer = jwtSigners.get();

        if (signer == null) {
            val jwtKey = properties.getJwtKey();
            signer = JWTSignerUtil.hs256(jwtKey.getBytes());
            jwtSigners.set(signer);
        }

        return signer;
    }

}
