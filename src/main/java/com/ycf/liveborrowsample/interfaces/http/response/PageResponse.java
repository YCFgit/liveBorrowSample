package com.ycf.liveborrowsample.interfaces.http.response;

import java.util.List;

public record PageResponse<T>(
    List<T> list,
    int pageNo,
    int pageSize,
    long total
) {
}
