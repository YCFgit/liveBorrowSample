package com.ycf.liveborrowsample.integration.gms;

import java.util.List;

public record GmsTransferOrder(
    String gmsOrderNo,
    TransferOrderType type,
    String bizNo,
    String fromOrgCode,
    String toOrgCode,
    String status,
    String logisticsCompanyName,
    String logisticsNo,
    List<CreateTransferOrderCommand.Item> items
) {
}
