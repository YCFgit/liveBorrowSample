package com.ycf.liveborrowsample.interfaces.http.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PickupConfirmRequest(
    @Valid @NotEmpty List<PickupConfirmItemRequest> items,
    String remark
) {
}
