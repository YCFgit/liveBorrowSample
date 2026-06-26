package com.ycf.liveborrowsample.domain.enums;

public enum BatchStatus implements CodeEnum {
    PENDING("PENDING", "待处理"),
    LOGISTICS_FILLED("LOGISTICS_FILLED", "已填写物流"),
    STORE_PENDING("STORE_PENDING", "待门店收货"),
    QUALITY_CHECKING("QUALITY_CHECKING", "质检中"),
    COMPLETED("COMPLETED", "已完成"),
    ABNORMAL_PENDING("ABNORMAL_PENDING", "异常待处理");

    private final String code;
    private final String desc;

    BatchStatus(String code, String desc) {
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
