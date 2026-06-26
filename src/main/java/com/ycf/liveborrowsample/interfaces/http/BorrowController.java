package com.ycf.liveborrowsample.interfaces.http;

import com.ycf.liveborrowsample.application.service.BorrowApplicationService;
import com.ycf.liveborrowsample.interfaces.http.request.BorrowApplicationCreateRequest;
import com.ycf.liveborrowsample.interfaces.http.request.PickupConfirmRequest;
import com.ycf.liveborrowsample.interfaces.http.request.ReceiveConfirmRequest;
import com.ycf.liveborrowsample.interfaces.http.request.ShipConfirmRequest;
import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowApplicationCreateResponse;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowTaskDetailResponse;
import com.ycf.liveborrowsample.interfaces.http.response.BorrowTaskListItemResponse;
import com.ycf.liveborrowsample.interfaces.http.response.OperationResultResponse;
import com.ycf.liveborrowsample.interfaces.http.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/borrow")
public class BorrowController {

    private final BorrowApplicationService borrowApplicationService;

    public BorrowController(BorrowApplicationService borrowApplicationService) {
        this.borrowApplicationService = borrowApplicationService;
    }

    @PostMapping("/applications")
    public ApiResponse<BorrowApplicationCreateResponse> createApplication(@Valid @RequestBody BorrowApplicationCreateRequest request) {
        return ApiResponse.success(borrowApplicationService.create(request));
    }

    @GetMapping("/tasks")
    public ApiResponse<PageResponse<BorrowTaskListItemResponse>> queryTasks(
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(borrowApplicationService.queryTasks(pageNo, pageSize, status));
    }

    @GetMapping("/tasks/{taskNo}")
    public ApiResponse<BorrowTaskDetailResponse> getTaskDetail(@PathVariable String taskNo) {
        return ApiResponse.success(borrowApplicationService.getTaskDetail(taskNo));
    }

    @PostMapping("/tasks/{taskNo}/receive-confirm")
    public ApiResponse<OperationResultResponse> confirmReceive(
        @PathVariable String taskNo,
        @Valid @RequestBody ReceiveConfirmRequest request
    ) {
        return ApiResponse.success(borrowApplicationService.confirmReceive(taskNo, request));
    }

    @PostMapping("/tasks/{taskNo}/ship-confirm")
    public ApiResponse<OperationResultResponse> confirmShip(
        @PathVariable String taskNo,
        @RequestBody ShipConfirmRequest request
    ) {
        return ApiResponse.success(borrowApplicationService.confirmShip(taskNo, request));
    }

    @PostMapping("/tasks/{taskNo}/pickup-confirm")
    public ApiResponse<OperationResultResponse> confirmPickup(
        @PathVariable String taskNo,
        @Valid @RequestBody PickupConfirmRequest request
    ) {
        return ApiResponse.success(borrowApplicationService.confirmPickup(taskNo, request));
    }

    @PostMapping("/tasks/{taskNo}/return-complete")
    public ApiResponse<OperationResultResponse> completeReturn(@PathVariable String taskNo) {
        return ApiResponse.success(borrowApplicationService.completeReturn(taskNo));
    }
}
