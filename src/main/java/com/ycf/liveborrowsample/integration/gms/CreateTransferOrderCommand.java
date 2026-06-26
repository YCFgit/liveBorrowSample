package com.ycf.liveborrowsample.integration.gms;

import java.util.List;

public record CreateTransferOrderCommand(
    TransferOrderType type,
    String bizNo,
    String fromOrgCode,
    String toOrgCode,
    List<Item> items
) {

    public record Item(
        String skuCode,
        String sizeCode,
        int quantity
    ) {
    }
}
