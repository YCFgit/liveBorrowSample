package com.ycf.liveborrowsample.infrastructure.persistence.entity;

public class ReturnAllocationEntity {

    private Long returnBatchId;
    private Long taskId;
    private Long taskItemId;
    private String taskNo;
    private String skuCode;
    private String sizeCode;
    private Integer allocatedQty;
    private String returnMethod;
    private Integer allocationSeq;

    public Long getReturnBatchId() {
        return returnBatchId;
    }

    public void setReturnBatchId(Long returnBatchId) {
        this.returnBatchId = returnBatchId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTaskItemId() {
        return taskItemId;
    }

    public void setTaskItemId(Long taskItemId) {
        this.taskItemId = taskItemId;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getSizeCode() {
        return sizeCode;
    }

    public void setSizeCode(String sizeCode) {
        this.sizeCode = sizeCode;
    }

    public Integer getAllocatedQty() {
        return allocatedQty;
    }

    public void setAllocatedQty(Integer allocatedQty) {
        this.allocatedQty = allocatedQty;
    }

    public String getReturnMethod() {
        return returnMethod;
    }

    public void setReturnMethod(String returnMethod) {
        this.returnMethod = returnMethod;
    }

    public Integer getAllocationSeq() {
        return allocationSeq;
    }

    public void setAllocationSeq(Integer allocationSeq) {
        this.allocationSeq = allocationSeq;
    }
}
