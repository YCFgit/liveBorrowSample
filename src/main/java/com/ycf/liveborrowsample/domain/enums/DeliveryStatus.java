package com.ycf.liveborrowsample.domain.enums;

public enum DeliveryStatus implements CodeEnum {
    NONE("NONE", "未开始"),
    WAITING("WAITING", "待发货"),
    SHIPPED("SHIPPED", "已发货"),
    SIGNED("SIGNED", "已签收"),
    RECEIVED("RECEIVED", "已收货");

    private final String code;
    private final String desc;

    DeliveryStatus(String code, String desc) {
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
