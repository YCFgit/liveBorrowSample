package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record DemoMetadataResponse(
    List<DemoUserResponse> users,
    List<VirtualStoreResponse> virtualShops,
    List<DemoStoreResponse> stores
) {
}
