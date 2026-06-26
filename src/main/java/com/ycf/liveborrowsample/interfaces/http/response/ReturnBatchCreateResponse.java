package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record ReturnBatchCreateResponse(
    String returnBatchNo,
    String status,
    String returnMethod,
    List<ReturnTaskSummaryResponse> taskSummaries
) {
}
