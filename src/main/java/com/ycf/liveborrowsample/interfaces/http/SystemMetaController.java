package com.ycf.liveborrowsample.interfaces.http;

import com.ycf.liveborrowsample.domain.enums.BatchStatus;
import com.ycf.liveborrowsample.domain.enums.CodeEnum;
import com.ycf.liveborrowsample.domain.enums.DeliveryStatus;
import com.ycf.liveborrowsample.domain.enums.DeliveryType;
import com.ycf.liveborrowsample.domain.enums.ExceptionStatus;
import com.ycf.liveborrowsample.domain.enums.PickupStatus;
import com.ycf.liveborrowsample.domain.enums.ReturnMethod;
import com.ycf.liveborrowsample.domain.enums.ReturnStatus;
import com.ycf.liveborrowsample.domain.enums.SampleFilterType;
import com.ycf.liveborrowsample.domain.enums.TaskStatus;
import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import com.ycf.liveborrowsample.interfaces.http.response.EnumItemResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/common")
public class SystemMetaController {

    @GetMapping("/enums")
    public ApiResponse<Map<String, List<EnumItemResponse>>> enums() {
        Map<String, List<EnumItemResponse>> result = new LinkedHashMap<>();
        result.put("deliveryTypes", toItems(DeliveryType.values()));
        result.put("deliveryStatuses", toItems(DeliveryStatus.values()));
        result.put("pickupStatuses", toItems(PickupStatus.values()));
        result.put("returnMethods", toItems(ReturnMethod.values()));
        result.put("returnStatuses", toItems(ReturnStatus.values()));
        result.put("sampleFilterTypes", toItems(SampleFilterType.values()));
        result.put("taskStatuses", toItems(TaskStatus.values()));
        result.put("exceptionStatuses", toItems(ExceptionStatus.values()));
        result.put("batchStatuses", toItems(BatchStatus.values()));
        return ApiResponse.success(result);
    }

    private List<EnumItemResponse> toItems(CodeEnum[] enums) {
        return Stream.of(enums)
            .map(item -> new EnumItemResponse(item.getCode(), item.getDesc()))
            .toList();
    }
}
