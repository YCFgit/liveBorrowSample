package com.ycf.liveborrowsample.domain.enums;

public enum ExceptionStatus implements CodeEnum {
    NONE("NONE", "无异常"),
    RECEIVE_DIFF("RECEIVE_DIFF", "收货差异"),
    RETURN_DIFF("RETURN_DIFF", "归还差异"),
    QUALITY_DOWNGRADED("QUALITY_DOWNGRADED", "品相降级"),
    GMS_SYNC_FAILED("GMS_SYNC_FAILED", "GMS 同步失败"),
    LOGISTICS_TIMEOUT("LOGISTICS_TIMEOUT", "物流超时");

    private final String code;
    private final String desc;

    ExceptionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
