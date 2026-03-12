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
package com.apzda.hajimi.openapi.provider;

import com.apzda.hajimi.openapi.domain.OpenAppClient;
import com.apzda.hajimi.openapi.service.IOpenAppClientService;
import com.apzda.kalami.common.openapi.ClientInfo;
import com.apzda.kalami.common.openapi.ClientInfoProvider;
import com.apzda.kalami.common.openapi.exception.IllegalClientIdException;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class AppBasedClientInfoProvider implements ClientInfoProvider {

    private final IOpenAppClientService openAppClientService;

    @Value("${openapi.test-client-id:}")
    private String testClientId;

    @Value("${openapi.server-public-key:}")
    private String serverPublicKey;

    @Value("${openapi.server-private-key:}")
    private String serverPrivateKey;

    @Nonnull
    private String getTestPriKey() {
        return """
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
    }

    @Nonnull
    private String getTestPubKey() {
        return """
                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl0wJ97DPljhvwx1bsaE1
                Ct+R2uVsqM7GbMuZWfaIPo+qnFk8kV1uT5KGKfRuyYoDCjvC7n8jB7gdJfqoE69V
                en0Lmd2+x/Oiko/ghYsHZ6vm2C9STEkQxnbJnk50cDoV0O+aa+il5iGFXOLGAQtU
                a4EoBXPtVJ27n0Vf7mGjXsDZ5iire9Igevrkgq/OGKHHdGJGTm2OiOw5Uo4Byko+
                xY6sMy1lRLsTts3EcHhBYcnbcch0Y+0+qEOgsfalkj3pQlleqthNdWLTKuMkax4b
                RewotMrryvjTvquWvcJT3j5MgU9nJQvDlPi1KBfQIZHGybgNQCcODaL7mDFi3SAy
                /wIDAQAB
                """;
    }

    @Override
    public ClientInfo getClientInfo(String clientId) throws IllegalClientIdException {
        ClientInfo clientInfo = new ClientInfo();
        if (clientId != null && clientId.equals(testClientId)) {
            clientInfo.setClientId(clientId);
            clientInfo.setClientSecret("12345678901234567890123456789012");
            clientInfo.setClientName("Test Client");
            clientInfo.setClientPrivateKey(getTestPriKey());
            clientInfo.setClientPublicKey(getTestPubKey());
            clientInfo.setServerPublicKey(getTestPubKey());
            clientInfo.setServerPrivateKey(getTestPriKey());
            clientInfo.setIpBlacklist("");
            clientInfo.setIpWhitelist("");
            clientInfo.setDisabled(false);
        }
        else {
            OpenAppClient client = openAppClientService.getByClientId(clientId);
            if (client == null) {
                throw new IllegalClientIdException();
            }

            clientInfo.setClientId(clientId);
            clientInfo.setClientSecret(client.getClientSecret());
            clientInfo.setClientName(client.getClientName());
            clientInfo.setClientPrivateKey(client.getClientPriKey());
            clientInfo.setClientPublicKey(client.getClientPubKey());
            clientInfo.setServerPublicKey(serverPublicKey);
            clientInfo.setServerPrivateKey(serverPrivateKey);
            clientInfo.setDisabled(Boolean.TRUE.equals(client.getDisabled()));
            val perm = openAppClientService.getPermByClientId(clientId);
            if (perm != null && perm.getPerm() != null) {
                clientInfo.setIpWhitelist(perm.getPerm().getIpWhiteList());
            }
        }

        return clientInfo;
    }

}
