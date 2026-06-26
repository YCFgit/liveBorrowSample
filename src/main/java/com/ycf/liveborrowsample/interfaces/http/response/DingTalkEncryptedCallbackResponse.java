package com.ycf.liveborrowsample.interfaces.http.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DingTalkEncryptedCallbackResponse(
    @JsonProperty("msg_signature") String msgSignature,
    @JsonProperty("timeStamp") String timeStamp,
    String nonce,
    String encrypt
) {
}
