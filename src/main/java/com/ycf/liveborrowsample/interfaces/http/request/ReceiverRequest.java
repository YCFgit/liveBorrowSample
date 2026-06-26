package com.ycf.liveborrowsample.interfaces.http.request;

public record ReceiverRequest(
    String name,
    String mobile,
    String province,
    String city,
    String district,
    String address
) {
}
