package com.ycf.liveborrowsample.interfaces.http.response;

public record ReturnBatchItemResponse(
    String skuCode,
    String sizeCode,
    String productName,
    String sampleType,
    int availableReturnQty,
    int applyReturnQty,
    String sourceStoreName
) {
}
