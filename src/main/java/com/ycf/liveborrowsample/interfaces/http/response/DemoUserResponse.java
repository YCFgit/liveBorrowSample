package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record DemoUserResponse(
    String userId,
    String userName,
    List<String> virtualShopIds
) {
}
