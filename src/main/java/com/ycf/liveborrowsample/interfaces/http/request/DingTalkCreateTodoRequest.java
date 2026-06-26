package com.ycf.liveborrowsample.interfaces.http.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DingTalkCreateTodoRequest(
    @NotBlank String unionId,
    String operatorId,
    @NotNull JsonNode payload
) {
}
