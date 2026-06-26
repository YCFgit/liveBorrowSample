package com.ycf.liveborrowsample.domain.model;

import com.ycf.liveborrowsample.domain.enums.DeliveryStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.enums.ExceptionStatus;
import com.ycf.liveborrowsample.domain.enums.PickupStatus;
import com.ycf.liveborrowsample.domain.enums.ReturnStatus;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SampleTask {

    private final Long id;
    private final String taskNo;
    private final String borrowNo;
    private final String applyNo;
    private final String applicantEmpId;
    private final String applicantName;
    private final String virtualStoreCode;
    private final String virtualStoreName;
    private final String sourceStoreName;
    private final DeliveryType deliveryType;
    private TaskStatus taskStatus;
    private DeliveryStatus deliveryStatus;
    private PickupStatus pickupStatus;
    private ReturnStatus returnStatus;
    private ExceptionStatus exceptionStatus;
    private String currentReturnBatchNo;
    private String logisticsNo;
    private final LocalDateTime borrowedAt;
    private final LocalDateTime expectedReturnAt;
    private final List<SampleTaskItem> items;

    public SampleTask(
        Long id,
        String taskNo,
        String borrowNo,
        String applyNo,
        String applicantEmpId,
        String applicantName,
        String virtualStoreCode,
        String virtualStoreName,
        String sourceStoreName,
        DeliveryType deliveryType,
        TaskStatus taskStatus,
        DeliveryStatus deliveryStatus,
        PickupStatus pickupStatus,
        ReturnStatus returnStatus,
        ExceptionStatus exceptionStatus,
        LocalDateTime borrowedAt,
        LocalDateTime expectedReturnAt,
        List<SampleTaskItem> items
    ) {
        this(
            id,
            taskNo,
            borrowNo,
            applyNo,
            applicantEmpId,
            applicantName,
            virtualStoreCode,
            virtualStoreName,
            sourceStoreName,
            deliveryType,
            taskStatus,
            deliveryStatus,
            pickupStatus,
            returnStatus,
            exceptionStatus,
            null,
            null,
            borrowedAt,
            expectedReturnAt,
            items
        );
    }

    public SampleTask(
        Long id,
        String taskNo,
        String borrowNo,
        String applyNo,
        String applicantEmpId,
        String applicantName,
        String virtualStoreCode,
        String virtualStoreName,
        String sourceStoreName,
        DeliveryType deliveryType,
        TaskStatus taskStatus,
        DeliveryStatus deliveryStatus,
        PickupStatus pickupStatus,
        ReturnStatus returnStatus,
        ExceptionStatus exceptionStatus,
        String currentReturnBatchNo,
        String logisticsNo,
        LocalDateTime borrowedAt,
        LocalDateTime expectedReturnAt,
        List<SampleTaskItem> items
    ) {
        this.id = id;
        this.taskNo = taskNo;
        this.borrowNo = borrowNo;
        this.applyNo = applyNo;
        this.applicantEmpId = applicantEmpId;
        this.applicantName = applicantName;
        this.virtualStoreCode = virtualStoreCode;
        this.virtualStoreName = virtualStoreName;
        this.sourceStoreName = sourceStoreName;
        this.deliveryType = deliveryType;
        this.taskStatus = taskStatus;
        this.deliveryStatus = deliveryStatus;
        this.pickupStatus = pickupStatus;
        this.returnStatus = returnStatus;
        this.exceptionStatus = exceptionStatus;
        this.currentReturnBatchNo = currentReturnBatchNo;
        this.logisticsNo = logisticsNo;
        this.borrowedAt = borrowedAt;
        this.expectedReturnAt = expectedReturnAt;
        this.items = new ArrayList<>(items);
    }

    public Long getId() {
        return id;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public String getBorrowNo() {
        return borrowNo;
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

    public String getSourceStoreName() {
        return sourceStoreName;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public PickupStatus getPickupStatus() {
        return pickupStatus;
    }

    public ReturnStatus getReturnStatus() {
        return returnStatus;
    }

    public ExceptionStatus getExceptionStatus() {
        return exceptionStatus;
    }

    public String getCurrentReturnBatchNo() {
        return currentReturnBatchNo;
    }

    public String getLogisticsNo() {
        return logisticsNo;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public LocalDateTime getExpectedReturnAt() {
        return expectedReturnAt;
    }

    public List<SampleTaskItem> getItems() {
        return items;
    }

    public void markInTransit(String logisticsNo) {
        this.taskStatus = TaskStatus.IN_TRANSIT;
        this.deliveryStatus = DeliveryStatus.SHIPPED;
        this.logisticsNo = logisticsNo;
    }

    public void confirmReceive(String logisticsNo) {
        this.taskStatus = TaskStatus.BORROWING;
        this.deliveryStatus = DeliveryStatus.RECEIVED;
        this.logisticsNo = logisticsNo;
        this.items.forEach(SampleTaskItem::confirmReceive);
    }

    public void confirmPickup(boolean fullyPicked) {
        this.pickupStatus = fullyPicked ? PickupStatus.PICKED : PickupStatus.PART_PICKED;
        if (fullyPicked) {
            this.taskStatus = TaskStatus.BORROWING;
        }
    }

    public void attachReturnBatch(String returnBatchNo, ReturnStatus newReturnStatus) {
        this.currentReturnBatchNo = returnBatchNo;
        this.returnStatus = newReturnStatus;
        this.taskStatus = TaskStatus.RETURNING;
    }

    public void fillReturnLogistics(String logisticsNo) {
        this.logisticsNo = logisticsNo;
        this.returnStatus = ReturnStatus.LOGISTICS_FILLED;
    }
}
