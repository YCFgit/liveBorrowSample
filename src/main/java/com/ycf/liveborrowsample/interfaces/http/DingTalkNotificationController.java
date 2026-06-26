package com.ycf.liveborrowsample.interfaces.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.ycf.liveborrowsample.application.service.DingTalkNotificationApplicationService;
import com.ycf.liveborrowsample.interfaces.http.request.DingTalkCardSendRequest;
import com.ycf.liveborrowsample.interfaces.http.request.DingTalkCreateTodoRequest;
import com.ycf.liveborrowsample.interfaces.http.request.DingTalkRobotBatchSendRequest;
import com.ycf.liveborrowsample.interfaces.http.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dingtalk")
public class DingTalkNotificationController {

    private final DingTalkNotificationApplicationService dingTalkNotificationApplicationService;

    public DingTalkNotificationController(DingTalkNotificationApplicationService dingTalkNotificationApplicationService) {
        this.dingTalkNotificationApplicationService = dingTalkNotificationApplicationService;
    }

    @PostMapping("/robot/messages/batch")
    public ApiResponse<JsonNode> sendRobotMessage(@Valid @RequestBody DingTalkRobotBatchSendRequest request) {
        return ApiResponse.success(dingTalkNotificationApplicationService.sendRobotMessage(request));
    }

    @PostMapping("/cards/create-and-deliver")
    public ApiResponse<JsonNode> createAndDeliverCard(@Valid @RequestBody DingTalkCardSendRequest request) {
        return ApiResponse.success(dingTalkNotificationApplicationService.createAndDeliverCard(request));
    }

    @PostMapping("/todos")
    public ApiResponse<JsonNode> createTodo(@Valid @RequestBody DingTalkCreateTodoRequest request) {
        return ApiResponse.success(dingTalkNotificationApplicationService.createTodo(request));
    }
}
