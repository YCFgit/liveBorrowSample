package com.ycf.liveborrowsample.integration.gms;

import java.util.List;
import java.util.Optional;

public interface GmsClient {

    List<AvailableInventory> queryAvailableInventory(String skuCode, String sizeCode);

    GmsTransferOrder createTransferOrder(CreateTransferOrderCommand command);

    Optional<GmsTransferOrder> getTransferOrder(String gmsOrderNo);

    GmsTransferOrder fillLogistics(String gmsOrderNo, String companyName, String logisticsNo);

    record AvailableInventory(
        String storeCode,
        String storeName,
        String skuCode,
        String sizeCode,
        int availableQty,
        String inventoryGrade
    ) {
    }
}
