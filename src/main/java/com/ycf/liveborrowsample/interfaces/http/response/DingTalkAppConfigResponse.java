package com.ycf.liveborrowsample.interfaces.http.response;

public record DingTalkAppConfigResponse(
    String appId,
    String agentId,
    String clientId,
    String robotCode,
    String corpId,
    String callbackUrl,
    boolean configured
) {
}
