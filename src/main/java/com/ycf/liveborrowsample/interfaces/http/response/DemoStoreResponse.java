package com.ycf.liveborrowsample.interfaces.http.response;

public record DemoStoreResponse(
    String storeId,
    String name,
    String address,
    String status
) {
}
