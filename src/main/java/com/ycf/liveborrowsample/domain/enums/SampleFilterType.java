package com.ycf.liveborrowsample.domain.enums;

public enum SampleFilterType implements CodeEnum {
    ALL("ALL", "全部样品"),
    PICKUP_ONLY("PICKUP_ONLY", "仅自提样品");

    private final String code;
    private final String desc;

    SampleFilterType(String code, String desc) {
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
