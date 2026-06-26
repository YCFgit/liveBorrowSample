package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.UUID;

public class ApiResponse<T> {

    private final String code;
    private final String message;
    private final String requestId;
    private final T data;

    private ApiResponse(String code, String message, String requestId, T data) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("0", "success", UUID.randomUUID().toString(), data);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(code, message, UUID.randomUUID().toString(), null);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestId() {
        return requestId;
    }

    public T getData() {
        return data;
    }
}
