package com.ycf.liveborrowsample.infrastructure.persistence.entity;

public class ReturnBatchItemEntity {

    private Long returnBatchId;
    private Integer lineNo;
    private String skuCode;
    private String sizeCode;
    private String productName;
    private String sampleType;
    private String sourceStoreName;
    private Integer availableReturnQty;
    private Integer applyReturnQty;

    public Long getReturnBatchId() {
        return returnBatchId;
    }

    public void setReturnBatchId(Long returnBatchId) {
        this.returnBatchId = returnBatchId;
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

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public String getSourceStoreName() {
        return sourceStoreName;
    }

    public void setSourceStoreName(String sourceStoreName) {
        this.sourceStoreName = sourceStoreName;
    }

    public Integer getAvailableReturnQty() {
        return availableReturnQty;
    }

    public void setAvailableReturnQty(Integer availableReturnQty) {
        this.availableReturnQty = availableReturnQty;
    }

    public Integer getApplyReturnQty() {
        return applyReturnQty;
    }

    public void setApplyReturnQty(Integer applyReturnQty) {
        this.applyReturnQty = applyReturnQty;
    }
}
