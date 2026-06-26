package com.ycf.liveborrowsample.interfaces.http.request;

public record ShipConfirmRequest(
    String logisticsCompany,
    String logisticsNo
) {
}
