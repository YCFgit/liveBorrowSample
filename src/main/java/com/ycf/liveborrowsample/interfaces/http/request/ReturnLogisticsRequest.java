package com.ycf.liveborrowsample.interfaces.http.request;

import jakarta.validation.constraints.NotBlank;

public record ReturnLogisticsRequest(
    @NotBlank String companyCode,
    @NotBlank String companyName,
    @NotBlank String logisticsNo
) {
}
