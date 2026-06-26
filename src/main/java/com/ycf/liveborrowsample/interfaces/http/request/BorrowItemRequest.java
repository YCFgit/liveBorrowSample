package com.ycf.liveborrowsample.interfaces.http.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BorrowItemRequest(
    @NotBlank String skuCode,
    @NotBlank String sizeCode,
    @Min(1) int applyQty
) {
}
