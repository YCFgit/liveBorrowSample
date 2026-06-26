package com.ycf.liveborrowsample.application.service;

import com.ycf.liveborrowsample.config.DingTalkProperties;
import com.ycf.liveborrowsample.domain.enums.ErrorCode;
import com.ycf.liveborrowsample.domain.exception.BusinessException;
import com.ycf.liveborrowsample.infrastructure.dingtalk.DingTalkApiException;
import com.ycf.liveborrowsample.infrastructure.dingtalk.DingTalkOpenApiClient;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkAppConfigResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkAuthUserResponse;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkJsapiConfigResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DingTalkApplicationService {

    private final DingTalkProperties properties;
    private final DingTalkOpenApiClient dingTalkOpenApiClient;

    public DingTalkApplicationService(DingTalkProperties properties, DingTalkOpenApiClient dingTalkOpenApiClient) {
        this.properties = properties;
        this.dingTalkOpenApiClient = dingTalkOpenApiClient;
    }

    public DingTalkAppConfigResponse getAppConfig() {
        return new DingTalkAppConfigResponse(
            properties.getAppId(),
            properties.getAgentId(),
            properties.getClientId(),
            properties.getRobotCode(),
            properties.getCorpId(),
            properties.getCallbackUrl(),
            properties.isConfigured()
        );
    }

    public DingTalkJsapiConfigResponse buildJsapiConfig(String url) {
        requireConfigured();
        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "url 不能为空");
        }
        try {
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            long timestamp = Instant.now().getEpochSecond();
            String ticket = dingTalkOpenApiClient.getJsapiTicket();
            String signature = sha1(
                "jsapi_ticket=" + ticket +
                    "&noncestr=" + nonceStr +
                    "&timestamp=" + timestamp +
                    "&url=" + url
            );
            return new DingTalkJsapiConfigResponse(
                properties.getAgentId(),
                properties.getClientId(),
                properties.getCorpId(),
                nonceStr,
                timestamp,
                signature,
                url
            );
        } catch (DingTalkApiException ex) {
            throw new BusinessException(ErrorCode.DINGTALK_API_FAILED, ex.getMessage());
        }
    }

    public DingTalkAuthUserResponse exchangeAuthCode(String code) {
        requireConfigured();
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "code 不能为空");
        }
        try {
            DingTalkOpenApiClient.UserByCodeResult result = dingTalkOpenApiClient.getUserInfoByCode(code);
            return new DingTalkAuthUserResponse(result.userId(), result.unionId(), result.name());
        } catch (DingTalkApiException ex) {
            throw new BusinessException(ErrorCode.DINGTALK_API_FAILED, ex.getMessage());
        }
    }

    private void requireConfigured() {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.DINGTALK_CONFIG_MISSING, "请先配置 DINGTALK_CLIENT_ID 和 DINGTALK_CLIENT_SECRET");
        }
    }

    private String sha1(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 not supported", ex);
        }
    }
}
