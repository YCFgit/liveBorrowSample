package com.ycf.liveborrowsample.interfaces.http.response;

public record DingTalkAuthUserResponse(
    String userId,
    String unionId,
    String name
) {
}
