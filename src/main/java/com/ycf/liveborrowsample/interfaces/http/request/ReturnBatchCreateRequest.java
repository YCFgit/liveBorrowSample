package com.ycf.liveborrowsample.interfaces.http.request;

import com.ycf.liveborrowsample.domain.enums.ReturnMethod;
import com.ycf.liveborrowsample.domain.enums.SampleFilterType;
import com.ycf.liveborrowsample.domain.enums.SourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReturnBatchCreateRequest(
    @NotBlank String virtualStoreCode,
    @NotNull SourceType sourceType,
    @NotNull SampleFilterType sampleFilterType,
    @NotNull ReturnMethod returnMethod,
    String remark,
    @Valid @NotEmpty List<ReturnBatchItemRequest> items
) {
}
