package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record ReturnAggregationRowResponse(
    String skuCode,
    String sizeCode,
    String productName,
    String sampleType,
    int availableReturnQty,
    String sourceStoreName,
    List<TaskRefResponse> taskRefs
) {
}
