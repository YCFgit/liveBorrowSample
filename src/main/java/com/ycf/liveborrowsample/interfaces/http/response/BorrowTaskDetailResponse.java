package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record BorrowTaskDetailResponse(
    String taskNo,
    String borrowNo,
    String applyNo,
    String applicantEmpId,
    String applicantName,
    String virtualStoreCode,
    String virtualStoreName,
    String sourceStoreName,
    String pickupStoreCode,
    String pickupStoreName,
    String receiverName,
    String receiverMobile,
    String receiverFullAddress,
    String remark,
    String deliveryType,
    String taskStatus,
    String deliveryStatus,
    String pickupStatus,
    String returnStatus,
    String currentReturnBatchNo,
    String logisticsNo,
    String borrowedAt,
    String expectedReturnAt,
    List<BorrowTaskItemResponse> items,
    List<String> actions
) {
}
