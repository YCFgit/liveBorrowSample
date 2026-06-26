package com.ycf.liveborrowsample.infrastructure.persistence.entity;

public class ReturnBatchEntity {

    private Long id;
    private String returnBatchNo;
    private String creatorEmpId;
    private String creatorName;
    private String virtualStoreCode;
    private String virtualStoreName;
    private String sourceType;
    private String sampleFilterType;
    private String returnMethod;
    private String status;
    private String logisticsCompanyName;
    private String logisticsNo;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReturnBatchNo() {
        return returnBatchNo;
    }

    public void setReturnBatchNo(String returnBatchNo) {
        this.returnBatchNo = returnBatchNo;
    }

    public String getCreatorEmpId() {
        return creatorEmpId;
    }

    public void setCreatorEmpId(String creatorEmpId) {
        this.creatorEmpId = creatorEmpId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getVirtualStoreCode() {
        return virtualStoreCode;
    }

    public void setVirtualStoreCode(String virtualStoreCode) {
        this.virtualStoreCode = virtualStoreCode;
    }

    public String getVirtualStoreName() {
        return virtualStoreName;
    }

    public void setVirtualStoreName(String virtualStoreName) {
        this.virtualStoreName = virtualStoreName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSampleFilterType() {
        return sampleFilterType;
    }

    public void setSampleFilterType(String sampleFilterType) {
        this.sampleFilterType = sampleFilterType;
    }

    public String getReturnMethod() {
        return returnMethod;
    }

    public void setReturnMethod(String returnMethod) {
        this.returnMethod = returnMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLogisticsCompanyName() {
        return logisticsCompanyName;
    }

    public void setLogisticsCompanyName(String logisticsCompanyName) {
        this.logisticsCompanyName = logisticsCompanyName;
    }

    public String getLogisticsNo() {
        return logisticsNo;
    }

    public void setLogisticsNo(String logisticsNo) {
        this.logisticsNo = logisticsNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
