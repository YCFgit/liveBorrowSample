package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record ReturnBatchDetailResponse(
    String returnBatchNo,
    String virtualStoreCode,
    String virtualStoreName,
    String sourceType,
    String sampleFilterType,
    String returnMethod,
    String status,
    String logisticsCompanyName,
    String logisticsNo,
    List<ReturnBatchItemResponse> items,
    List<ReturnAllocationResponse> allocations
) {
}
