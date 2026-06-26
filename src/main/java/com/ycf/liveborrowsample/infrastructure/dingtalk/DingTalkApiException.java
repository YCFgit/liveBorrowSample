package com.ycf.liveborrowsample.infrastructure.dingtalk;

public class DingTalkApiException extends RuntimeException {

    public DingTalkApiException(String message) {
        super(message);
    }
}
