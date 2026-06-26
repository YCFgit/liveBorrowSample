package com.ycf.liveborrowsample.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycf.liveborrowsample.config.DingTalkProperties;
import com.ycf.liveborrowsample.domain.enums.ErrorCode;
import com.ycf.liveborrowsample.domain.exception.BusinessException;
import com.ycf.liveborrowsample.infrastructure.dingtalk.DingTalkApiException;
import com.ycf.liveborrowsample.infrastructure.dingtalk.DingTalkOpenApiClient;
import com.ycf.liveborrowsample.interfaces.http.request.DingTalkCardSendRequest;
import com.ycf.liveborrowsample.interfaces.http.request.DingTalkCreateTodoRequest;
import com.ycf.liveborrowsample.interfaces.http.request.DingTalkRobotBatchSendRequest;
import org.springframework.stereotype.Service;

@Service
public class DingTalkNotificationApplicationService {

    private final DingTalkProperties properties;
    private final DingTalkOpenApiClient client;
    private final ObjectMapper objectMapper;

    public DingTalkNotificationApplicationService(
        DingTalkProperties properties,
        DingTalkOpenApiClient client,
        ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public JsonNode sendRobotMessage(DingTalkRobotBatchSendRequest request) {
        requireConfigured();
        String robotCode = request.robotCode();
        if (robotCode == null || robotCode.isBlank()) {
            robotCode = properties.getRobotCode();
        }
        if (robotCode == null || robotCode.isBlank()) {
            throw new BusinessException(ErrorCode.DINGTALK_CONFIG_MISSING, "请先配置 DINGTALK_ROBOT_CODE");
        }
        try {
            return client.sendRobotOtoBatchMessage(
                robotCode,
                request.userIds(),
                request.msgKey(),
                toJsonString(request.msgParam())
            );
        } catch (DingTalkApiException ex) {
            throw new BusinessException(ErrorCode.DINGTALK_API_FAILED, ex.getMessage());
        }
    }

    public JsonNode createAndDeliverCard(DingTalkCardSendRequest request) {
        requireConfigured();
        try {
            return client.createAndDeliverCard(request.payload());
        } catch (DingTalkApiException ex) {
            throw new BusinessException(ErrorCode.DINGTALK_API_FAILED, ex.getMessage());
        }
    }

    public JsonNode createTodo(DingTalkCreateTodoRequest request) {
        requireConfigured();
        try {
            return client.createTodoTask(request.unionId(), request.operatorId(), request.payload());
        } catch (DingTalkApiException ex) {
            throw new BusinessException(ErrorCode.DINGTALK_API_FAILED, ex.getMessage());
        }
    }

    private void requireConfigured() {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.DINGTALK_CONFIG_MISSING, "请先配置 DINGTALK_CLIENT_ID 和 DINGTALK_CLIENT_SECRET");
        }
    }

    private String toJsonString(JsonNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "msgParam 必须是合法 JSON");
        }
    }
}
