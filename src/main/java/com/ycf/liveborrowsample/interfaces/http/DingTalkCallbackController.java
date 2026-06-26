package com.ycf.liveborrowsample.interfaces.http;

import com.ycf.liveborrowsample.application.service.DingTalkCallbackApplicationService;
import com.ycf.liveborrowsample.infrastructure.dingtalk.DingTalkCallbackCrypto;
import com.ycf.liveborrowsample.interfaces.http.request.DingTalkCallbackRequest;
import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkCallbackEventResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkEncryptedCallbackResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dingtalk/callback")
public class DingTalkCallbackController {

    private final DingTalkCallbackApplicationService dingTalkCallbackApplicationService;

    public DingTalkCallbackController(DingTalkCallbackApplicationService dingTalkCallbackApplicationService) {
        this.dingTalkCallbackApplicationService = dingTalkCallbackApplicationService;
    }

    @PostMapping
    public DingTalkEncryptedCallbackResponse receiveCallback(
        @RequestParam(required = false) String signature,
        @RequestParam(required = false, name = "msg_signature") String msgSignature,
        @RequestParam String timestamp,
        @RequestParam String nonce,
        @Valid @RequestBody DingTalkCallbackRequest request
    ) {
        DingTalkCallbackCrypto.EncryptedCallbackResponse response =
            dingTalkCallbackApplicationService.handleEvent(resolveSignature(signature, msgSignature), timestamp, nonce, request.encrypt());
        return new DingTalkEncryptedCallbackResponse(
            response.msgSignature(),
            response.timeStamp(),
            response.nonce(),
            response.encrypt()
        );
    }

    @GetMapping("/last-event")
    public ApiResponse<DingTalkCallbackEventResponse> getLastEvent() {
        return ApiResponse.success(dingTalkCallbackApplicationService.getLastEvent());
    }

    private String resolveSignature(String signature, String msgSignature) {
        if (signature != null && !signature.isBlank()) {
            return signature;
        }
        return msgSignature;
    }
}
