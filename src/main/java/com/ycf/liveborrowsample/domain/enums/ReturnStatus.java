package com.ycf.liveborrowsample.domain.enums;

public enum ReturnStatus implements CodeEnum {
    NONE("NONE", "未归还"),
    PENDING("PENDING", "已发起待处理"),
    LOGISTICS_FILLED("LOGISTICS_FILLED", "已填写物流"),
    STORE_PENDING("STORE_PENDING", "待门店收货"),
    WAREHOUSE_RECEIVED("WAREHOUSE_RECEIVED", "仓库已收货"),
    QUALITY_CHECKING("QUALITY_CHECKING", "质检中"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String desc;

    ReturnStatus(String code, String desc) {
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
