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
package com.apzda.kalami.common.openapi;

import com.apzda.kalami.common.openapi.exception.OpenApiException;
import com.apzda.kalami.common.openapi.sign.ClientBasedSha256Signer;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
class OpenApiCertificateBasedSignerTest {
    private static ClientInfoProvider provider;
    private static final String serverPublicKey = """
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl0wJ97DPljhvwx1bsaE1
        Ct+R2uVsqM7GbMuZWfaIPo+qnFk8kV1uT5KGKfRuyYoDCjvC7n8jB7gdJfqoE69V
        en0Lmd2+x/Oiko/ghYsHZ6vm2C9STEkQxnbJnk50cDoV0O+aa+il5iGFXOLGAQtU
        a4EoBXPtVJ27n0Vf7mGjXsDZ5iire9Igevrkgq/OGKHHdGJGTm2OiOw5Uo4Byko+
        xY6sMy1lRLsTts3EcHhBYcnbcch0Y+0+qEOgsfalkj3pQlleqthNdWLTKuMkax4b
        RewotMrryvjTvquWvcJT3j5MgU9nJQvDlPi1KBfQIZHGybgNQCcODaL7mDFi3SAy
        /wIDAQAB
        """;
    private static final String serverPrivateKey = """
        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCXTAn3sM+WOG/D
        HVuxoTUK35Ha5WyozsZsy5lZ9og+j6qcWTyRXW5PkoYp9G7JigMKO8LufyMHuB0l
        +qgTr1V6fQuZ3b7H86KSj+CFiwdnq+bYL1JMSRDGdsmeTnRwOhXQ75pr6KXmIYVc
        4sYBC1RrgSgFc+1UnbufRV/uYaNewNnmKKt70iB6+uSCr84Yocd0YkZObY6I7DlS
        jgHKSj7FjqwzLWVEuxO2zcRweEFhydtxyHRj7T6oQ6Cx9qWSPelCWV6q2E11YtMq
        4yRrHhtF7Ci0yuvK+NO+q5a9wlPePkyBT2clC8OU+LUoF9AhkcbJuA1AJw4NovuY
        MWLdIDL/AgMBAAECggEAIRzvy4mjjU2xzELXKDPPn58Z1Y5D71g+Hl6bJBDsdcDM
        xuZS9r+7nmfDvFf2jV6K1skIm1kxCgX2PzMyaQiFTUcj7FuXXzzH9orNyXyJtNtv
        LR6CRRbyuHUUIHUsT8mur6lZI0fqU0k/3nn1exIHOnYsk7DxHwVcpHmzCsHM1zZI
        xRzcGw8k6Yh3tNjwAHk1IkMzS1zHQfyHWspkmhVWt6ntTtAy6JgB0qE/hqiK3WfE
        +mo7pYjMWVeo1MQkNkxl/dIoPIeCs+d2gnjlEZU3UOIZBAMCFGF+Ej98h0z2aINI
        IAqKlw9fhDdw37awdokNPzMpweYLXRxo1hLC01ElgQKBgQC4R8k36TadY7oS9XEV
        EVFGJES0OGZnQxPWPT28daoyjvktx8sTy2AxtWw4xhOyWjfQqhEvGnu3qV3lQ31I
        iBQ53aokuCui1yHq+/Q8SazZL62ccJXQME4zc1eV6rBHkUpJYOYIzUEQfV0rMplX
        nyevXekiRohAihlFPS+DgKbQfwKBgQDSLg3mo5E9vXH3lrXwm7TRGiNFokQJ/2mn
        Nih0TChPciO27iVYoDPHn7dxvAHi/tl0CMa8E72LD/7U/a0qxb3FqdeR/ywmMqn8
        yi8fV909OwMpQPe3+s1r2mUmaCdD7sILeT7+2LB2QaGIvhfjJ60ImBKLQZYSDjgs
        8g97ty1dgQKBgQCf6mvLgS5L1scavLOtPKHy5HZajrcO1T7Zo7t3WASia0ABYmfw
        fYS0nmOTwRBGrdPR2EOhRLIn52n8El0eHDixHpMP2mLDpT1h1dNUnfrJXn6iS3E2
        gTTboTRYfJkNAiAdHWGCMdxMmv3HBtnbF9owbKZrgBmtKCpV04rv6yzXsQKBgEwo
        Wesfi6pGcfxXxNdJZEx8XxmFQYgp31uyeRgi3FxlLGHFiwzBB+Imkm8Iw9o/pAWy
        hGhz8rNn+VuuC8g63Je6Ah2py5KPux6ZGFgQG19rearCGsC0hSi2ev5esROhePh1
        V4k6dT50a5bD4p3xq3Y4vEdvXXRvaJWIpGmcpziBAoGAQtzYlHouNHK4FMKqtmfW
        /4YHZPQO0KzU5o8WAulxeTGup7YbJdbZ1BCN6KiDAd/0E8hSVNwA3R0VnPPhXNJL
        vN83S2ZR3URZOWsfbeNtr6DJsLhRUdbCbhvfeqNFuVTT+8N9QXvuik6IUlhKSc/f
        q+/+avDWuUqOr3mlicU/tKo=
        """;

    @BeforeAll
    static void beforeAll() {
        val clientInfo = new ClientInfo();
        clientInfo.setClientSecret("");
        clientInfo.setClientPrivateKey(serverPrivateKey);
        clientInfo.setClientPublicKey(serverPublicKey);
        clientInfo.setServerPublicKey(serverPublicKey);
        clientInfo.setServerPrivateKey(serverPrivateKey);
        provider = clientId -> clientInfo;
    }

    @org.junit.jupiter.api.Test
    void verify() throws OpenApiException {
        val head = new Request.Head();
        head.setClientId("1111");
        head.setTimestamp(123456789L);

        val signature = "2780be416f0a9be8b14ec59334f10a02116e9b918630e463eded39c27ff0b61f";
        val content = "Hello World!你好，世界";
        val singer = new ClientBasedSha256Signer(provider);
        Assertions.assertDoesNotThrow(() -> {
            singer.verify(head, content.getBytes(StandardCharsets.UTF_8), signature);
        });
    }

    @org.junit.jupiter.api.Test
    void sign() throws OpenApiException {
        val singer = new ClientBasedSha256Signer(provider);
        val head = new Response.Head();
        head.setTimestamp(123456789L);
        val content = "Hello World!你好，世界";

        val sign = singer.sign(head, content.getBytes(StandardCharsets.UTF_8));
        assertThat(sign).isEqualTo("2780be416f0a9be8b14ec59334f10a02116e9b918630e463eded39c27ff0b61f");
    }
}
