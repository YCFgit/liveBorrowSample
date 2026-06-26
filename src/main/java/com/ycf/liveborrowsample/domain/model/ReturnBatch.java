package com.ycf.liveborrowsample.domain.model;

import com.ycf.liveborrowsample.domain.enums.BatchStatus;
import com.ycf.liveborrowsample.domain.enums.ReturnMethod;
import com.ycf.liveborrowsample.domain.enums.SampleFilterType;
import com.ycf.liveborrowsample.domain.enums.SourceType;
import java.util.ArrayList;
import java.util.List;

public class ReturnBatch {

    private final String returnBatchNo;
    private final String virtualStoreCode;
    private final String virtualStoreName;
    private final SourceType sourceType;
    private final SampleFilterType sampleFilterType;
    private final ReturnMethod returnMethod;
    private BatchStatus status;
    private String logisticsCompanyName;
    private String logisticsNo;
    private final List<ReturnBatchItem> items;
    private final List<ReturnAllocation> allocations;

    public ReturnBatch(
        String returnBatchNo,
        String virtualStoreCode,
        String virtualStoreName,
        SourceType sourceType,
        SampleFilterType sampleFilterType,
        ReturnMethod returnMethod,
        BatchStatus status,
        List<ReturnBatchItem> items,
        List<ReturnAllocation> allocations
    ) {
        this(
            returnBatchNo,
            virtualStoreCode,
            virtualStoreName,
            sourceType,
            sampleFilterType,
            returnMethod,
            status,
            null,
            null,
            items,
            allocations
        );
    }

    public ReturnBatch(
        String returnBatchNo,
        String virtualStoreCode,
        String virtualStoreName,
        SourceType sourceType,
        SampleFilterType sampleFilterType,
        ReturnMethod returnMethod,
        BatchStatus status,
        String logisticsCompanyName,
        String logisticsNo,
        List<ReturnBatchItem> items,
        List<ReturnAllocation> allocations
    ) {
        this.returnBatchNo = returnBatchNo;
        this.virtualStoreCode = virtualStoreCode;
        this.virtualStoreName = virtualStoreName;
        this.sourceType = sourceType;
        this.sampleFilterType = sampleFilterType;
        this.returnMethod = returnMethod;
        this.status = status;
        this.logisticsCompanyName = logisticsCompanyName;
        this.logisticsNo = logisticsNo;
        this.items = new ArrayList<>(items);
        this.allocations = new ArrayList<>(allocations);
    }

    public String getReturnBatchNo() {
        return returnBatchNo;
    }

    public String getVirtualStoreCode() {
        return virtualStoreCode;
    }

    public String getVirtualStoreName() {
        return virtualStoreName;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public SampleFilterType getSampleFilterType() {
        return sampleFilterType;
    }

    public ReturnMethod getReturnMethod() {
        return returnMethod;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public String getLogisticsCompanyName() {
        return logisticsCompanyName;
    }

    public String getLogisticsNo() {
        return logisticsNo;
    }

    public List<ReturnBatchItem> getItems() {
        return items;
    }

    public List<ReturnAllocation> getAllocations() {
        return allocations;
    }

    public void fillLogistics(String logisticsCompanyName, String logisticsNo) {
        this.logisticsCompanyName = logisticsCompanyName;
        this.logisticsNo = logisticsNo;
        this.status = BatchStatus.LOGISTICS_FILLED;
    }

    public void complete() {
        this.status = BatchStatus.COMPLETED;
    }
}
