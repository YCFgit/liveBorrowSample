package com.ycf.liveborrowsample.interfaces.http;

import com.ycf.liveborrowsample.application.service.ReturnApplicationService;
import com.ycf.liveborrowsample.domain.enums.SampleFilterType;
import com.ycf.liveborrowsample.interfaces.http.request.ReturnBatchCreateRequest;
import com.ycf.liveborrowsample.interfaces.http.request.ReturnLogisticsRequest;
import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnAggregationResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnBatchCreateResponse;
import com.ycf.liveborrowsample.interfaces.http.response.ReturnBatchDetailResponse;
import com.ycf.liveborrowsample.interfaces.http.response.VirtualStoreResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/returns")
public class ReturnController {

    private final ReturnApplicationService returnApplicationService;

    public ReturnController(ReturnApplicationService returnApplicationService) {
        this.returnApplicationService = returnApplicationService;
    }

    @GetMapping("/virtual-stores")
    public ApiResponse<List<VirtualStoreResponse>> queryVirtualStores() {
        return ApiResponse.success(returnApplicationService.queryVirtualStores());
    }

    @GetMapping("/aggregations")
    public ApiResponse<ReturnAggregationResponse> queryAggregations(
        @RequestParam String virtualStoreCode,
        @RequestParam SampleFilterType sampleFilterType
    ) {
        return ApiResponse.success(returnApplicationService.queryAggregations(virtualStoreCode, sampleFilterType));
    }

    @PostMapping("/batches")
    public ApiResponse<ReturnBatchCreateResponse> createBatch(@Valid @RequestBody ReturnBatchCreateRequest request) {
        return ApiResponse.success(returnApplicationService.createBatch(request));
    }

    @GetMapping("/batches/{returnBatchNo}")
    public ApiResponse<ReturnBatchDetailResponse> getBatchDetail(@PathVariable String returnBatchNo) {
        return ApiResponse.success(returnApplicationService.getBatchDetail(returnBatchNo));
    }

    @PostMapping("/batches/{returnBatchNo}/logistics")
    public ApiResponse<ReturnBatchDetailResponse> fillLogistics(
        @PathVariable String returnBatchNo,
        @Valid @RequestBody ReturnLogisticsRequest request
    ) {
        return ApiResponse.success(returnApplicationService.fillLogistics(returnBatchNo, request));
    }
}
