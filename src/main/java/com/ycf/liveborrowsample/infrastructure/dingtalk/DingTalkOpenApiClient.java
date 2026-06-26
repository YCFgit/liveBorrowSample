package com.ycf.liveborrowsample.infrastructure.dingtalk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.ycf.liveborrowsample.config.DingTalkProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class DingTalkOpenApiClient {

    private final RestClient restClient;
    private final DingTalkProperties properties;
    private final Map<String, CachedValue> cache = new ConcurrentHashMap<>();

    public DingTalkOpenApiClient(DingTalkProperties properties) {
        this.restClient = RestClient.builder().build();
        this.properties = properties;
    }

    public String getAccessToken() {
        return getOrLoad("access_token", () -> {
            GetTokenResponse response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("api.dingtalk.com")
                    .path("/v1.0/oauth2/accessToken")
                    .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "appKey", properties.getClientId(),
                    "appSecret", properties.getClientSecret()
                ))
                .retrieve()
                .body(GetTokenResponse.class);
            if (response == null || response.accessToken() == null) {
                throw new DingTalkApiException("获取 access_token 失败: " + (response == null ? "empty response" : firstNonBlank(response.errMsg(), response.message())));
            }
            return new TokenValue(response.accessToken(), response.expireIn() == null ? 7200L : response.expireIn());
        });
    }

    public String getJsapiTicket() {
        return getOrLoad("jsapi_ticket", () -> {
            JsapiTicketResponse response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                    .scheme("https")
                    .host("api.dingtalk.com")
                    .path("/v1.0/oauth2/jsapiTickets")
                    .build())
                .header("x-acs-dingtalk-access-token", getAccessToken())
                .retrieve()
                .body(JsapiTicketResponse.class);
            if (response == null || response.jsapiTicket() == null) {
                throw new DingTalkApiException("获取 jsapi_ticket 失败: " + (response == null ? "empty response" : response.message()));
            }
            return new TokenValue(response.jsapiTicket(), response.expireIn() == null ? 7200L : response.expireIn());
        });
    }

    public UserByCodeResult getUserInfoByCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);

        GetUserInfoByCodeResponse response = restClient.post()
            .uri(uriBuilder -> uriBuilder
                .scheme("https")
                .host("oapi.dingtalk.com")
                .path("/topapi/v2/user/getuserinfo")
                .queryParam("access_token", getAccessToken())
                .build())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(GetUserInfoByCodeResponse.class);

        if (response == null || response.result() == null || response.errcode() != 0) {
            throw new DingTalkApiException("免登 code 换取用户信息失败: " + (response == null ? "empty response" : response.errmsg()));
        }

        return new UserByCodeResult(
            response.result().userid(),
            response.result().unionid(),
            response.result().name()
        );
    }

    public JsonNode sendRobotOtoBatchMessage(String robotCode, List<String> userIds, String msgKey, String msgParam) {
        JsonNode response = restClient.post()
            .uri("https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend")
            .header("x-acs-dingtalk-access-token", getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of(
                "robotCode", robotCode,
                "userIds", userIds,
                "msgKey", msgKey,
                "msgParam", msgParam
            ))
            .retrieve()
            .body(JsonNode.class);
        if (response == null) {
            throw new DingTalkApiException("机器人消息发送失败: empty response");
        }
        return response;
    }

    public JsonNode createAndDeliverCard(JsonNode payload) {
        JsonNode response = restClient.post()
            .uri("https://api.dingtalk.com/v1.0/card/instances/createAndDeliver")
            .header("x-acs-dingtalk-access-token", getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .body(JsonNode.class);
        if (response == null) {
            throw new DingTalkApiException("创建并投放卡片失败: empty response");
        }
        return response;
    }

    public JsonNode createTodoTask(String unionId, String operatorId, JsonNode payload) {
        JsonNode response = restClient.post()
            .uri(uriBuilder -> {
                var builder = uriBuilder
                    .scheme("https")
                    .host("api.dingtalk.com")
                    .path("/v1.0/todo/users/{unionId}/tasks");
                if (operatorId != null && !operatorId.isBlank()) {
                    builder.queryParam("operatorId", operatorId);
                }
                return builder.build(unionId);
            })
            .header("x-acs-dingtalk-access-token", getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .body(JsonNode.class);
        if (response == null) {
            throw new DingTalkApiException("创建待办失败: empty response");
        }
        return response;
    }

    private String getOrLoad(String key, ValueSupplier supplier) {
        CachedValue cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(Instant.now())) {
            return cached.value();
        }

        TokenValue tokenValue = supplier.get();
        Instant expiresAt = Instant.now().plusSeconds(Math.max(tokenValue.expiresIn() - 300L, 60L));
        cache.put(key, new CachedValue(tokenValue.value(), expiresAt));
        return tokenValue.value();
    }

    private interface ValueSupplier {
        TokenValue get();
    }

    private record TokenValue(String value, long expiresIn) {
    }

    private record CachedValue(String value, Instant expiresAt) {
    }

    public record UserByCodeResult(String userId, String unionId, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GetTokenResponse(String accessToken, Long expireIn, String errMsg, String code, String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record JsapiTicketResponse(String jsapiTicket, Long expireIn, String code, String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GetUserInfoByCodeResponse(Integer errcode, String errmsg, UserByCodeInnerResult result) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record UserByCodeInnerResult(String userid, String unionid, String name) {
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
