package com.ycf.liveborrowsample.application.service;

import com.ycf.liveborrowsample.interfaces.http.response.BorrowTaskListItemResponse;
import com.ycf.liveborrowsample.interfaces.http.response.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class AdminApplicationService {

    private final BorrowApplicationService borrowApplicationService;

    public AdminApplicationService(BorrowApplicationService borrowApplicationService) {
        this.borrowApplicationService = borrowApplicationService;
    }

    public PageResponse<BorrowTaskListItemResponse> queryTasks(int pageNo, int pageSize, String taskStatus) {
        return borrowApplicationService.queryTasks(pageNo, pageSize, taskStatus);
    }
}
