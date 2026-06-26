package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record ReturnAggregationResponse(
    String virtualStoreCode,
    String sampleFilterType,
    List<ReturnAggregationRowResponse> rows
) {
}
