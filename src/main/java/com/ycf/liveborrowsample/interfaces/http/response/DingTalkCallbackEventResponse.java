package com.ycf.liveborrowsample.interfaces.http.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record DingTalkCallbackEventResponse(
    String eventType,
    LocalDateTime receivedAt,
    JsonNode payload
) {
}
