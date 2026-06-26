package com.ycf.liveborrowsample.infrastructure.persistence.entity;

public class SampleTaskItemEntity {

    private Long id;
    private Long taskId;
    private Integer lineNo;
    private String skuCode;
    private String sizeCode;
    private String productName;
    private String inventoryGrade;
    private Integer applyQty;
    private Integer approvedQty;
    private Integer shippedQty;
    private Integer receivedQty;
    private Integer pickedQty;
    private Integer borrowingQty;
    private Integer returnedApplyQty;
    private Integer returnedReceivedQty;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getInventoryGrade() {
        return inventoryGrade;
    }

    public void setInventoryGrade(String inventoryGrade) {
        this.inventoryGrade = inventoryGrade;
    }

    public Integer getApplyQty() {
        return applyQty;
    }

    public void setApplyQty(Integer applyQty) {
        this.applyQty = applyQty;
    }

    public Integer getApprovedQty() {
        return approvedQty;
    }

    public void setApprovedQty(Integer approvedQty) {
        this.approvedQty = approvedQty;
    }

    public Integer getShippedQty() {
        return shippedQty;
    }

    public void setShippedQty(Integer shippedQty) {
        this.shippedQty = shippedQty;
    }

    public Integer getReceivedQty() {
        return receivedQty;
    }

    public void setReceivedQty(Integer receivedQty) {
        this.receivedQty = receivedQty;
    }

    public Integer getPickedQty() {
        return pickedQty;
    }

    public void setPickedQty(Integer pickedQty) {
        this.pickedQty = pickedQty;
    }

    public Integer getBorrowingQty() {
        return borrowingQty;
    }

    public void setBorrowingQty(Integer borrowingQty) {
        this.borrowingQty = borrowingQty;
    }

    public Integer getReturnedApplyQty() {
        return returnedApplyQty;
    }

    public void setReturnedApplyQty(Integer returnedApplyQty) {
        this.returnedApplyQty = returnedApplyQty;
    }

    public Integer getReturnedReceivedQty() {
        return returnedReceivedQty;
    }

    public void setReturnedReceivedQty(Integer returnedReceivedQty) {
        this.returnedReceivedQty = returnedReceivedQty;
    }
}
