package com.ycf.liveborrowsample.domain.enums;

public enum TaskStatus implements CodeEnum {
    CREATED("CREATED", "已创建"),
    AUDITING("AUDITING", "审核中"),
    SOURCING("SOURCING", "寻源中"),
    TRANSFER_CREATING("TRANSFER_CREATING", "建单中"),
    WAIT_SHIP("WAIT_SHIP", "待发货"),
    WAIT_PICKUP("WAIT_PICKUP", "待自提"),
    IN_TRANSIT("IN_TRANSIT", "运输中"),
    BORROWING("BORROWING", "借样中"),
    RETURNING("RETURNING", "归还中"),
    QUALITY_CHECKING("QUALITY_CHECKING", "质检中"),
    COMPLETED("COMPLETED", "已完成"),
    ABNORMAL_PENDING("ABNORMAL_PENDING", "异常待处理"),
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String desc;

    TaskStatus(String code, String desc) {
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
