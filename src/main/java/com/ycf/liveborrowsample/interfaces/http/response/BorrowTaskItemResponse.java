package com.ycf.liveborrowsample.interfaces.http.response;

public record BorrowTaskItemResponse(
    Long taskItemId,
    String skuCode,
    String sizeCode,
    String productName,
    int approvedQty,
    int shippedQty,
    int receivedQty,
    int pickedQty,
    int borrowingQty,
    int returnedApplyQty
) {
}
