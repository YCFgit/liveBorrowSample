package com.ycf.liveborrowsample.domain.enums;

public enum SourceType implements CodeEnum {
    TASK_DETAIL("TASK_DETAIL", "任务详情发起"),
    GENERAL_RETURN("GENERAL_RETURN", "通用归还发起");

    private final String code;
    private final String desc;

    SourceType(String code, String desc) {
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
