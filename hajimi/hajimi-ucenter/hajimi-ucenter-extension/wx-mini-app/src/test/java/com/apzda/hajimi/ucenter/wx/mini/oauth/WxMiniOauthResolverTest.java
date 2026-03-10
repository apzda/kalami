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
package com.apzda.hajimi.ucenter.wx.mini.oauth;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
class WxMiniOauthResolverTest {

    @Test
    void testResolveOpenId() {
        String openId = "opkpCvvJ0Myz4W1Eq8e09S5wZAI";
        WxMiniOauthResolver resolver = new WxMiniOauthResolver(null);
        val id = resolver.resolveOpenId(openId);

        assertThat(id).isEqualTo("opkpCvvJ0Myz4W1Eq8e09S5wZAI");
    }

    @Test
    void testResolveOpenId1() {
        String openId = "opkpCvvJ0Myz4W-1Eq8e09S5wZAI-15221112898";
        WxMiniOauthResolver resolver = new WxMiniOauthResolver(null);
        val id = resolver.resolveOpenId(openId);

        assertThat(id).isEqualTo("opkpCvvJ0Myz4W-1Eq8e09S5wZAI");
    }

    @Test
    void testResolveOpenId2() {
        String openId = "opkpCvvJ0Myz4W-1Eq8e09S5wZAI";
        WxMiniOauthResolver resolver = new WxMiniOauthResolver(null);
        val id = resolver.resolveOpenId(openId);

        assertThat(id).isEqualTo("opkpCvvJ0Myz4W-1Eq8e09S5wZAI");
    }

}
