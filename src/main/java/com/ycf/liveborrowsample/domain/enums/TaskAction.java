package com.ycf.liveborrowsample.domain.enums;

public enum TaskAction implements CodeEnum {
    SUBMIT_APPLY("SUBMIT_APPLY", "提交借样申请"),
    APPROVE_APPLY("APPROVE_APPLY", "审核通过"),
    REJECT_APPLY("REJECT_APPLY", "审核拒绝"),
    CREATE_OUTBOUND_TRANSFER("CREATE_OUTBOUND_TRANSFER", "创建借出调拨单"),
    CONFIRM_RECEIVE("CONFIRM_RECEIVE", "确认收货"),
    CONFIRM_PICKUP("CONFIRM_PICKUP", "确认自提"),
    CREATE_RETURN_BATCH("CREATE_RETURN_BATCH", "发起归还批次"),
    FILL_RETURN_LOGISTICS("FILL_RETURN_LOGISTICS", "填写归还物流"),
    CONFIRM_STORE_RECEIVE("CONFIRM_STORE_RECEIVE", "门店确认收货"),
    COMPLETE_QUALITY_CHECK("COMPLETE_QUALITY_CHECK", "完成质检");

    private final String code;
    private final String desc;

    TaskAction(String code, String desc) {
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
