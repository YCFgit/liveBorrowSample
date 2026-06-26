package com.ycf.liveborrowsample.interfaces.http.response;

import java.time.LocalDateTime;

public class HealthResponse {

    private final String service;
    private final String status;
    private final LocalDateTime now;

    public HealthResponse(String service, String status, LocalDateTime now) {
        this.service = service;
        this.status = status;
        this.now = now;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getNow() {
        return now;
    }
}
