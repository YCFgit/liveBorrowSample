package com.ycf.liveborrowsample.interfaces.http.response;

public record BorrowTaskListItemResponse(
    String taskNo,
    String borrowNo,
    String virtualStoreCode,
    String virtualStoreName,
    String sourceStoreName,
    String deliveryType,
    String taskStatus,
    String deliveryStatus,
    String pickupStatus,
    String returnStatus,
    String expectedReturnAt,
    String logisticsNo,
    String itemSummary
) {
}
