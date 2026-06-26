package com.ycf.liveborrowsample.domain.enums;

public enum ReturnMethod implements CodeEnum {
    EXPRESS("EXPRESS", "快递归还"),
    IN_PERSON("IN_PERSON", "自行归还");

    private final String code;
    private final String desc;

    ReturnMethod(String code, String desc) {
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
