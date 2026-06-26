package com.ycf.liveborrowsample.infrastructure.persistence.entity;

import java.time.LocalDateTime;

public class SampleTaskEntity {

    private Long id;
    private String taskNo;
    private String borrowNo;
    private String applyNo;
    private String applicantEmpId;
    private String applicantName;
    private String virtualStoreCode;
    private String virtualStoreName;
    private String sourceStoreName;
    private String deliveryType;
    private String taskStatus;
    private String deliveryStatus;
    private String pickupStatus;
    private String returnStatus;
    private String exceptionStatus;
    private String currentReturnBatchNo;
    private String logisticsNo;
    private LocalDateTime borrowedAt;
    private LocalDateTime expectedReturnAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getBorrowNo() {
        return borrowNo;
    }

    public void setBorrowNo(String borrowNo) {
        this.borrowNo = borrowNo;
    }

    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public String getApplicantEmpId() {
        return applicantEmpId;
    }

    public void setApplicantEmpId(String applicantEmpId) {
        this.applicantEmpId = applicantEmpId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
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

    public String getSourceStoreName() {
        return sourceStoreName;
    }

    public void setSourceStoreName(String sourceStoreName) {
        this.sourceStoreName = sourceStoreName;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getPickupStatus() {
        return pickupStatus;
    }

    public void setPickupStatus(String pickupStatus) {
        this.pickupStatus = pickupStatus;
    }

    public String getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(String returnStatus) {
        this.returnStatus = returnStatus;
    }

    public String getExceptionStatus() {
        return exceptionStatus;
    }

    public void setExceptionStatus(String exceptionStatus) {
        this.exceptionStatus = exceptionStatus;
    }

    public String getCurrentReturnBatchNo() {
        return currentReturnBatchNo;
    }

    public void setCurrentReturnBatchNo(String currentReturnBatchNo) {
        this.currentReturnBatchNo = currentReturnBatchNo;
    }

    public String getLogisticsNo() {
        return logisticsNo;
    }

    public void setLogisticsNo(String logisticsNo) {
        this.logisticsNo = logisticsNo;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    public LocalDateTime getExpectedReturnAt() {
        return expectedReturnAt;
    }

    public void setExpectedReturnAt(LocalDateTime expectedReturnAt) {
        this.expectedReturnAt = expectedReturnAt;
    }
}
