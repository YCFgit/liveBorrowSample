package com.ycf.liveborrowsample.infrastructure.persistence.entity;

public class BorrowApplicationEntity {

    private Long id;
    private String applyNo;
    private String applicantEmpId;
    private String applicantName;
    private String virtualStoreCode;
    private String virtualStoreName;
    private String deliveryType;
    private String pickupStoreCode;
    private String pickupStoreName;
    private String receiverName;
    private String receiverMobile;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String auditStatus;
    private String sourceChannel;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getPickupStoreCode() {
        return pickupStoreCode;
    }

    public void setPickupStoreCode(String pickupStoreCode) {
        this.pickupStoreCode = pickupStoreCode;
    }

    public String getPickupStoreName() {
        return pickupStoreName;
    }

    public void setPickupStoreName(String pickupStoreName) {
        this.pickupStoreName = pickupStoreName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public String getReceiverProvince() {
        return receiverProvince;
    }

    public void setReceiverProvince(String receiverProvince) {
        this.receiverProvince = receiverProvince;
    }

    public String getReceiverCity() {
        return receiverCity;
    }

    public void setReceiverCity(String receiverCity) {
        this.receiverCity = receiverCity;
    }

    public String getReceiverDistrict() {
        return receiverDistrict;
    }

    public void setReceiverDistrict(String receiverDistrict) {
        this.receiverDistrict = receiverDistrict;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(String sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
