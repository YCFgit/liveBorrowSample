package com.ycf.liveborrowsample.interfaces.http.response;

public record TaskRefResponse(
    String taskNo,
    Long taskItemId,
    String borrowedAt,
    int remainingQty
) {
}
