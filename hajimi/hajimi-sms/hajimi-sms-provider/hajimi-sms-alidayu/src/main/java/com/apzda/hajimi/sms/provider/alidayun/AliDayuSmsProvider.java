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

import cn.hutool.core.collection.CollectionUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.apzda.hajimi.sms.core.SmsProvider;
import com.apzda.hajimi.sms.core.config.ProviderProperties;
import com.apzda.hajimi.sms.core.dto.Sms;
import com.apzda.hajimi.sms.core.dto.Variable;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class AliDayuSmsProvider implements SmsProvider {

    public static final String DAYU = "dayu";

    private boolean testMode;

    private IAcsClient acsClient;

    @Override
    @Nonnull
    public String getId() {
        return DAYU;
    }

    @Override
    public void init(@Nonnull ProviderProperties props) throws Exception {
        if (!props.isEnabled() || props.isTestMode()) {
            this.testMode = true;
            return;
        }
        val accessKey = props.getAccessKey();
        val secretKey = props.getSecretKey();
        val regionId = props.getRegionId();
        Assert.isTrue(StringUtils.isNotBlank(accessKey), "accessKey is blank");
        Assert.isTrue(StringUtils.isNotBlank(secretKey), "secretKey is blank");
        Assert.isTrue(StringUtils.isNotBlank(regionId), "regionId is blank");
        this.testMode = props.isTestMode();
        if (this.testMode) {
            return;
        }
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKey, secretKey);
        acsClient = new DefaultAcsClient(profile);
    }

    @Override
    public boolean send(@Nonnull Sms sms) throws Exception {
        if (testMode || sms.isTestMode()) {
            return true;
        }

        val sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setPhoneNumbers(sms.getPhone());
        sendSmsRequest.setSignName(sms.getSignName());
        sendSmsRequest.setTemplateCode(sms.getTemplateId());
        if (sms.getSmsLogId() != null) {
            sendSmsRequest.setOutId(sms.getSmsLogId().toString());
        }
        val variables = sms.getVariables();
        if (CollectionUtil.isNotEmpty(variables)) {
            sendSmsRequest.setTemplateParam(Variable.toJsonStr(variables));
        }
        val sendSmsResponse = acsClient.getAcsResponse(sendSmsRequest);
        if (log.isDebugEnabled()) {
            log.debug("Dayu: {Code:{},Message:{},RequestId:{},BizId:{}}", sendSmsResponse.getCode(),
                    sendSmsResponse.getMessage(), sendSmsResponse.getRequestId(), sendSmsResponse.getBizId());
        }
        return "OK".equals(sendSmsResponse.getCode());
    }

}
