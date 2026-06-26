package com.ycf.liveborrowsample.infrastructure.dingtalk;

import com.ycf.liveborrowsample.config.DingTalkProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DingTalkCallbackCryptoTest {

    @Test
    void shouldRoundTripCallbackPayload() {
        DingTalkProperties properties = new DingTalkProperties();
        properties.setClientId("ding4bi4qdiazkljxe9y");
        properties.setCallbackToken("token123");
        properties.setCallbackAesKey("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG");

        DingTalkCallbackCrypto crypto = new DingTalkCallbackCrypto(properties);
        DingTalkCallbackCrypto.EncryptedCallbackResponse response = crypto.encryptResponse("success", "1783610513", "nonce123");

        String decrypted = crypto.decrypt(
            response.msgSignature(),
            response.timeStamp(),
            response.nonce(),
            response.encrypt()
        );

        assertEquals("success", decrypted);
    }
}
