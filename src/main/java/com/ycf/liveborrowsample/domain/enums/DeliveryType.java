package com.ycf.liveborrowsample.domain.enums;

public enum DeliveryType implements CodeEnum {
    EXPRESS("EXPRESS", "快递"),
    PICKUP("PICKUP", "自提");

    private final String code;
    private final String desc;

    DeliveryType(String code, String desc) {
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
