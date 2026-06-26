package com.ycf.liveborrowsample.interfaces.http.request;

import jakarta.validation.constraints.NotBlank;

public record ReceiveConfirmRequest(
    @NotBlank String logisticsNo,
    String remark
) {
}
