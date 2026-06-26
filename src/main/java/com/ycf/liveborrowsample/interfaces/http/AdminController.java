package com.ycf.liveborrowsample.interfaces.http;

import com.ycf.liveborrowsample.application.service.AdminApplicationService;
import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowTaskListItemResponse;
import com.ycf.liveborrowsample.interfaces.http.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminApplicationService adminApplicationService;

    public AdminController(AdminApplicationService adminApplicationService) {
        this.adminApplicationService = adminApplicationService;
    }

    @GetMapping("/tasks")
    public ApiResponse<PageResponse<BorrowTaskListItemResponse>> queryTasks(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String taskStatus
    ) {
        return ApiResponse.success(adminApplicationService.queryTasks(pageNo, pageSize, taskStatus));
    }
}
