package com.ycf.liveborrowsample.domain.model;

public class SampleTaskItem {

    private final Long id;
    private final String skuCode;
    private final String sizeCode;
    private final String productName;
    private final String inventoryGrade;
    private final int approvedQty;
    private int shippedQty;
    private int receivedQty;
    private int pickedQty;
    private int borrowingQty;
    private int returnedApplyQty;
    private int returnedReceivedQty;

    public SampleTaskItem(
        Long id,
        String skuCode,
        String sizeCode,
        String productName,
        String inventoryGrade,
        int approvedQty,
        int shippedQty,
        int receivedQty,
        int pickedQty,
        int borrowingQty
    ) {
        this(
            id,
            skuCode,
            sizeCode,
            productName,
            inventoryGrade,
            approvedQty,
            shippedQty,
            receivedQty,
            pickedQty,
            borrowingQty,
            0,
            0
        );
    }

    public SampleTaskItem(
        Long id,
        String skuCode,
        String sizeCode,
        String productName,
        String inventoryGrade,
        int approvedQty,
        int shippedQty,
        int receivedQty,
        int pickedQty,
        int borrowingQty,
        int returnedApplyQty,
        int returnedReceivedQty
    ) {
        this.id = id;
        this.skuCode = skuCode;
        this.sizeCode = sizeCode;
        this.productName = productName;
        this.inventoryGrade = inventoryGrade;
        this.approvedQty = approvedQty;
        this.shippedQty = shippedQty;
        this.receivedQty = receivedQty;
        this.pickedQty = pickedQty;
        this.borrowingQty = borrowingQty;
        this.returnedApplyQty = returnedApplyQty;
        this.returnedReceivedQty = returnedReceivedQty;
    }

    public Long getId() {
        return id;
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

    public String getInventoryGrade() {
        return inventoryGrade;
    }

    public int getApprovedQty() {
        return approvedQty;
    }

    public int getShippedQty() {
        return shippedQty;
    }

    public int getReceivedQty() {
        return receivedQty;
    }

    public int getPickedQty() {
        return pickedQty;
    }

    public int getBorrowingQty() {
        return borrowingQty;
    }

    public int getReturnedApplyQty() {
        return returnedApplyQty;
    }

    public int getReturnedReceivedQty() {
        return returnedReceivedQty;
    }

    public int getRemainingReturnQty() {
        return borrowingQty - returnedApplyQty;
    }

    public void confirmPickup(int qty) {
        this.pickedQty += qty;
        this.borrowingQty += qty;
    }

    public void confirmReceive() {
        this.borrowingQty = this.receivedQty > 0 ? this.receivedQty : this.borrowingQty;
    }

    public void applyReturn(int qty) {
        this.returnedApplyQty += qty;
    }
}
