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
import com.ycf.liveborrowsample.interfaces.http.response.DemoMetadataResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DemoStoreResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DemoUserResponse;
import com.ycf.liveborrowsample.interfaces.http.response.EnumItemResponse;
import com.ycf.liveborrowsample.interfaces.http.response.VirtualStoreResponse;
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

    private static final List<DemoStoreResponse> DEMO_STORES = List.of(
        new DemoStoreResponse("ST001", "上海万象城店", "闵行区吴中路1599号", "active"),
        new DemoStoreResponse("ST002", "北京三里屯店", "朝阳区三里屯路19号", "active"),
        new DemoStoreResponse("ST003", "杭州银泰店", "西湖区延安路258号", "active"),
        new DemoStoreResponse("ST004", "深圳万象天地店", "南山区深南大道9668号", "active"),
        new DemoStoreResponse("ST005", "成都春熙路店", "锦江区春熙路68号", "active"),
        new DemoStoreResponse("ST006", "广州天河城店", "天河区天河路208号", "active"),
        new DemoStoreResponse("ST007", "已关门测试店", "某地", "closed")
    );

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

    @GetMapping("/demo-metadata")
    public ApiResponse<DemoMetadataResponse> demoMetadata() {
        List<VirtualStoreResponse> virtualShops = List.of(
            new VirtualStoreResponse("VS0001", "直播虚店一号"),
            new VirtualStoreResponse("VS0002", "直播虚店二号")
        );
        return ApiResponse.success(new DemoMetadataResponse(
            List.of(
                new DemoUserResponse("zhangsan", "张三", List.of("VS0001")),
                new DemoUserResponse("lisi", "李四", List.of("VS0002"))
            ),
            virtualShops,
            DEMO_STORES
        ));
    }

    private List<EnumItemResponse> toItems(CodeEnum[] enums) {
        return Stream.of(enums)
            .map(item -> new EnumItemResponse(item.getCode(), item.getDesc()))
            .toList();
    }
}
