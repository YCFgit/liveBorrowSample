package com.ycf.liveborrowsample.interfaces.http.request;

import jakarta.validation.constraints.NotBlank;

public record DingTalkCallbackRequest(
    @NotBlank String encrypt
) {
}
