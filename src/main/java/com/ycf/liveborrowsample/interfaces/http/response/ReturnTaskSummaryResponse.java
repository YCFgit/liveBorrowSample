package com.ycf.liveborrowsample.interfaces.http.response;

public record ReturnTaskSummaryResponse(
    String taskNo,
    int allocatedQty
) {
}
