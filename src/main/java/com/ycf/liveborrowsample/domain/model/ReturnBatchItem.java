package com.ycf.liveborrowsample.domain.model;

public class ReturnBatchItem {

    private final String skuCode;
    private final String sizeCode;
    private final String productName;
    private final String sampleType;
    private final int availableReturnQty;
    private final int applyReturnQty;
    private final String sourceStoreName;

    public ReturnBatchItem(
        String skuCode,
        String sizeCode,
        String productName,
        String sampleType,
        int availableReturnQty,
        int applyReturnQty,
        String sourceStoreName
    ) {
        this.skuCode = skuCode;
        this.sizeCode = sizeCode;
        this.productName = productName;
        this.sampleType = sampleType;
        this.availableReturnQty = availableReturnQty;
        this.applyReturnQty = applyReturnQty;
        this.sourceStoreName = sourceStoreName;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public String getSizeCode() {
        return sizeCode;
    }

    public String getProductName() {
        return productName;
    }

    public String getSampleType() {
        return sampleType;
    }

    public int getAvailableReturnQty() {
        return availableReturnQty;
    }

    public int getApplyReturnQty() {
        return applyReturnQty;
    }

    public String getSourceStoreName() {
        return sourceStoreName;
    }
}
