package com.ycf.liveborrowsample.interfaces.http.response;

public record OperationResultResponse(
    String bizNo,
    String taskStatus,
    String deliveryStatus,
    String pickupStatus,
    String returnStatus,
    String message
) {
}
