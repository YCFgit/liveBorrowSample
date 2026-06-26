package com.ycf.liveborrowsample.interfaces.http.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record DingTalkCardSendRequest(
    @NotNull JsonNode payload
) {
}
