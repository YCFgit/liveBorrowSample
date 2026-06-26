package com.ycf.liveborrowsample.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycf.liveborrowsample.infrastructure.dingtalk.DingTalkCallbackCrypto;
import com.ycf.liveborrowsample.interfaces.http.response.DingTalkCallbackEventResponse;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class DingTalkCallbackApplicationService {

    private final DingTalkCallbackCrypto callbackCrypto;
    private final ObjectMapper objectMapper;
    private final AtomicReference<DingTalkCallbackEventResponse> lastEvent = new AtomicReference<>();

    public DingTalkCallbackApplicationService(DingTalkCallbackCrypto callbackCrypto, ObjectMapper objectMapper) {
        this.callbackCrypto = callbackCrypto;
        this.objectMapper = objectMapper;
    }

    public DingTalkCallbackCrypto.EncryptedCallbackResponse handleEvent(String signature, String timestamp, String nonce, String encrypted) {
        String plainText = callbackCrypto.decrypt(signature, timestamp, nonce, encrypted);
        JsonNode payload = parse(plainText);
        String eventType = payload.path("EventType").asText(payload.path("eventType").asText("UNKNOWN"));
        lastEvent.set(new DingTalkCallbackEventResponse(eventType, LocalDateTime.now(), payload));
        return callbackCrypto.encryptResponse("success", timestamp, nonce);
    }

    public DingTalkCallbackEventResponse getLastEvent() {
        return lastEvent.get();
    }

    private JsonNode parse(String plainText) {
        try {
            return objectMapper.readTree(plainText);
        } catch (Exception ex) {
            return objectMapper.createObjectNode().put("raw", plainText);
        }
    }
}
