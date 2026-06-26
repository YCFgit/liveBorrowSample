package com.ycf.liveborrowsample.domain.enums;

public enum PickupStatus implements CodeEnum {
    NOT_APPLICABLE("NOT_APPLICABLE", "不适用"),
    WAIT_PICKUP("WAIT_PICKUP", "待自提"),
    PART_PICKED("PART_PICKED", "部分已提"),
    PICKED("PICKED", "已提货");

    private final String code;
    private final String desc;

    PickupStatus(String code, String desc) {
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
