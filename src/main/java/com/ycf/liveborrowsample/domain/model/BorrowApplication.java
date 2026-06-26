package com.ycf.liveborrowsample.domain.model;

import com.ycf.liveborrowsample.domain.enums.AuditStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import java.util.ArrayList;
import java.util.List;

public class BorrowApplication {

    private final String applyNo;
    private final String applicantEmpId;
    private final String applicantName;
    private final String virtualStoreCode;
    private final String virtualStoreName;
    private final DeliveryType deliveryType;
    private final String pickupStoreCode;
    private final String pickupStoreName;
    private final String receiverName;
    private final String receiverMobile;
    private final String receiverProvince;
    private final String receiverCity;
    private final String receiverDistrict;
    private final String receiverAddress;
    private final AuditStatus auditStatus;
    private final String sourceChannel;
    private final String remark;
    private final List<BorrowApplicationItem> items;

    public BorrowApplication(
        String applyNo,
        String applicantEmpId,
        String applicantName,
        String virtualStoreCode,
        String virtualStoreName,
        DeliveryType deliveryType,
        String pickupStoreCode,
        String pickupStoreName,
        String receiverName,
        String receiverMobile,
        String receiverProvince,
        String receiverCity,
        String receiverDistrict,
        String receiverAddress,
        AuditStatus auditStatus,
        String sourceChannel,
        String remark,
        List<BorrowApplicationItem> items
    ) {
        this.applyNo = applyNo;
        this.applicantEmpId = applicantEmpId;
        this.applicantName = applicantName;
        this.virtualStoreCode = virtualStoreCode;
        this.virtualStoreName = virtualStoreName;
        this.deliveryType = deliveryType;
        this.pickupStoreCode = pickupStoreCode;
        this.pickupStoreName = pickupStoreName;
        this.receiverName = receiverName;
        this.receiverMobile = receiverMobile;
        this.receiverProvince = receiverProvince;
        this.receiverCity = receiverCity;
        this.receiverDistrict = receiverDistrict;
        this.receiverAddress = receiverAddress;
        this.auditStatus = auditStatus;
        this.sourceChannel = sourceChannel;
        this.remark = remark;
        this.items = new ArrayList<>(items);
    }

    public String getApplyNo() {
        return applyNo;
    }

    public String getApplicantEmpId() {
        return applicantEmpId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public String getVirtualStoreCode() {
        return virtualStoreCode;
    }

    public String getVirtualStoreName() {
        return virtualStoreName;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public String getPickupStoreCode() {
        return pickupStoreCode;
    }

    public String getPickupStoreName() {
        return pickupStoreName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public String getReceiverProvince() {
        return receiverProvince;
    }

    public String getReceiverCity() {
        return receiverCity;
    }

    public String getReceiverDistrict() {
        return receiverDistrict;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public AuditStatus getAuditStatus() {
        return auditStatus;
    }

    public String getSourceChannel() {
        return sourceChannel;
    }

    public String getRemark() {
        return remark;
    }

    public List<BorrowApplicationItem> getItems() {
        return items;
    }
}
