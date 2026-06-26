package com.ycf.liveborrowsample.domain.model;

public class BorrowApplicationItem {

    private final int lineNo;
    private final String skuCode;
    private final String sizeCode;
    private final String productName;
    private final int applyQty;
    private final int approvedQty;

    public BorrowApplicationItem(
        int lineNo,
        String skuCode,
        String sizeCode,
        String productName,
        int applyQty,
        int approvedQty
    ) {
        this.lineNo = lineNo;
        this.skuCode = skuCode;
        this.sizeCode = sizeCode;
        this.productName = productName;
        this.applyQty = applyQty;
        this.approvedQty = approvedQty;
    }

    public int getLineNo() {
        return lineNo;
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

    public int getApplyQty() {
        return applyQty;
    }

    public int getApprovedQty() {
        return approvedQty;
    }
}
