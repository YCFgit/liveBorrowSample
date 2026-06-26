package com.ycf.liveborrowsample.interfaces.http.request;

import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record BorrowApplicationCreateRequest(
    @NotBlank String virtualStoreCode,
    @NotNull DeliveryType deliveryType,
    String pickupStoreCode,
    String remark,
    ReceiverRequest receiver,
    @Valid @NotEmpty List<BorrowItemRequest> items
) {
}
