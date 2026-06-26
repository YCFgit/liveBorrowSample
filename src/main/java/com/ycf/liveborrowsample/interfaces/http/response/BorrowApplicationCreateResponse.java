package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record BorrowApplicationCreateResponse(
    String applyNo,
    String auditStatus,
    String borrowNo,
    List<String> taskNos
) {
}
