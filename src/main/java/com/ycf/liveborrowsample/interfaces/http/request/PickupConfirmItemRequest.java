package com.ycf.liveborrowsample.interfaces.http.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PickupConfirmItemRequest(
    @NotNull Long taskItemId,
    @Min(1) int confirmQty
) {
}
