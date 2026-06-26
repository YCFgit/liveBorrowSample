package com.ycf.liveborrowsample.interfaces.http.response;

public class EnumItemResponse {

    private final String code;
    private final String desc;

    public EnumItemResponse(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
