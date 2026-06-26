package com.ycf.liveborrowsample.domain.model;

import com.ycf.liveborrowsample.domain.enums.ReturnMethod;

public class ReturnAllocation {

    private final String taskNo;
    private final Long taskItemId;
    private final String skuCode;
    private final String sizeCode;
    private final int allocatedQty;
    private final ReturnMethod returnMethod;
    private final int allocationSeq;

    public ReturnAllocation(
        String taskNo,
        Long taskItemId,
        String skuCode,
        String sizeCode,
        int allocatedQty,
        ReturnMethod returnMethod,
        int allocationSeq
    ) {
        this.taskNo = taskNo;
        this.taskItemId = taskItemId;
        this.skuCode = skuCode;
        this.sizeCode = sizeCode;
        this.allocatedQty = allocatedQty;
        this.returnMethod = returnMethod;
        this.allocationSeq = allocationSeq;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public Long getTaskItemId() {
        return taskItemId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public String getSizeCode() {
        return sizeCode;
    }

    public int getAllocatedQty() {
        return allocatedQty;
    }

    public ReturnMethod getReturnMethod() {
        return returnMethod;
    }

    public int getAllocationSeq() {
        return allocationSeq;
    }
}
