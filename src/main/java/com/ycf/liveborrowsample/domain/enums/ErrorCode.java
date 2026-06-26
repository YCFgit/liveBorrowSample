package com.ycf.liveborrowsample.domain.enums;

public enum ErrorCode implements CodeEnum {
    SUCCESS("0", "success"),
    SYSTEM_ERROR("SYS_001", "系统异常"),
    PARAM_INVALID("SYS_002", "参数错误"),
    TASK_ACTION_ILLEGAL("TASK_001", "当前状态不可执行此操作"),
    TASK_CONCURRENT_MODIFY("TASK_002", "并发更新失败，请重试"),
    BORROW_NO_PERMISSION("BORROW_001", "无借样权限"),
    BORROW_OVERDUE_EXISTS("BORROW_002", "存在逾期借样"),
    BORROW_OVER_LIMIT("BORROW_003", "已超过借样上限"),
    BORROW_BLACKLISTED("BORROW_004", "商品命中黑名单"),
    BORROW_INVENTORY_NOT_ENOUGH("BORROW_005", "库存不足"),
    RETURN_NOT_ENOUGH("RETURN_001", "可还数量不足"),
    RETURN_METHOD_ILLEGAL("RETURN_002", "归还方式非法"),
    RETURN_BATCH_STATUS_ILLEGAL("RETURN_003", "当前批次不可填写物流"),
    DINGTALK_CONFIG_MISSING("DINGTALK_001", "钉钉配置缺失"),
    DINGTALK_API_FAILED("DINGTALK_002", "钉钉接口调用失败"),
    GMS_CREATE_FAILED("GMS_001", "GMS 建单失败"),
    GMS_LOGISTICS_FILL_FAILED("GMS_002", "GMS 回填物流失败");

    private final String code;
    private final String desc;

    ErrorCode(String code, String desc) {
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
