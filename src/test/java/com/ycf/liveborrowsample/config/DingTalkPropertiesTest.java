package com.ycf.liveborrowsample.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DingTalkPropertiesTest {

    @Test
    void shouldPreferCorpIdAsCallbackOwnerKeyForInternalApp() {
        DingTalkProperties properties = new DingTalkProperties();
        properties.setClientId("ding-client-id");
        properties.setCorpId("ding-corp-id");

        assertEquals("ding-corp-id", properties.resolveCallbackOwnerKey());
    }

    @Test
    void shouldPreferExplicitCallbackOwnerKeyWhenProvided() {
        DingTalkProperties properties = new DingTalkProperties();
        properties.setClientId("ding-client-id");
        properties.setCorpId("ding-corp-id");
        properties.setCallbackOwnerKey("explicit-owner-key");

        assertEquals("explicit-owner-key", properties.resolveCallbackOwnerKey());
    }
}
