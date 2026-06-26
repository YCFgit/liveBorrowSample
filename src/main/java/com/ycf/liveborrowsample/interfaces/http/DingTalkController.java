package com.ycf.liveborrowsample.interfaces.http;

import com.ycf.liveborrowsample.application.service.DingTalkApplicationService;
import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkAppConfigResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkAuthUserResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkJsapiConfigResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dingtalk")
public class DingTalkController {

    private final DingTalkApplicationService dingTalkApplicationService;

    public DingTalkController(DingTalkApplicationService dingTalkApplicationService) {
        this.dingTalkApplicationService = dingTalkApplicationService;
    }

    @GetMapping("/app-config")
    public ApiResponse<DingTalkAppConfigResponse> getAppConfig() {
        return ApiResponse.success(dingTalkApplicationService.getAppConfig());
    }

    @GetMapping("/jsapi-config")
    public ApiResponse<DingTalkJsapiConfigResponse> getJsapiConfig(@RequestParam String url) {
        return ApiResponse.success(dingTalkApplicationService.buildJsapiConfig(url));
    }

    @PostMapping("/auth/exchange")
    public ApiResponse<DingTalkAuthUserResponse> exchangeAuthCode(@RequestParam String code) {
        return ApiResponse.success(dingTalkApplicationService.exchangeAuthCode(code));
    }
}
