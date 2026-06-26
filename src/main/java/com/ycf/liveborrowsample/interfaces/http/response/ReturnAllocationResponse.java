package com.ycf.liveborrowsample.interfaces.http.response;

public record ReturnAllocationResponse(
    String taskNo,
    Long taskItemId,
    String skuCode,
    String sizeCode,
    int allocatedQty,
    String returnMethod,
    int allocationSeq
) {
}
