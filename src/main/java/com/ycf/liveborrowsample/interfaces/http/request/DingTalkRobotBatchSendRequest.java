package com.ycf.liveborrowsample.interfaces.http.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DingTalkRobotBatchSendRequest(
    String robotCode,
    @NotEmpty List<@NotBlank String> userIds,
    @NotBlank String msgKey,
    @NotNull JsonNode msgParam
) {
}
