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
package com.apzda.hajimi.sms.provider.alidayun;

import com.apzda.hajimi.sms.core.config.ProviderProperties;
import com.apzda.hajimi.sms.core.dto.Sms;
import com.apzda.hajimi.sms.core.dto.Variable;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Disabled
class AliDayuSmsProviderTest {

    @Test
    void send() throws Exception {
        // given
        val pp = new ProviderProperties();
        pp.setAccessKey("LTAxxxxxxxxxxxxxx");
        pp.setSecretKey("KsG9KWbyyyyyyyyy");
        pp.setRegionId("cn-hangzhou");
        val smsProvider = new AliDayuSmsProvider();
        smsProvider.init(pp);
        val sms = new Sms();
        sms.setPhone("18088888888");
        sms.setSignName("签名");
        sms.setTemplateId("SMS_123456789");
        sms.setVariables(List.of(new Variable("code", "123456", 0)));

        // when
        var result = smsProvider.send(sms);

        // then
        assertThat(result).isTrue();
    }

}
