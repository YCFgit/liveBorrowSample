package com.ycf.liveborrowsample.interfaces.http.response;

public record DingTalkJsapiConfigResponse(
    String agentId,
    String clientId,
    String corpId,
    String nonceStr,
    long timeStamp,
    String signature,
    String url
) {
}
