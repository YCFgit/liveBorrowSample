package com.ycf.liveborrowsample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dingtalk")
public class DingTalkProperties {

    private String appId;
    private String agentId;
    private String clientId;
    private String clientSecret;
    private String robotCode;
    private String corpId;
    private String callbackToken;
    private String callbackAesKey;
    private String callbackOwnerKey;
    private String callbackUrl;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRobotCode() {
        return robotCode;
    }

    public void setRobotCode(String robotCode) {
        this.robotCode = robotCode;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String corpId) {
        this.corpId = corpId;
    }

    public String getCallbackToken() {
        return callbackToken;
    }

    public void setCallbackToken(String callbackToken) {
        this.callbackToken = callbackToken;
    }

    public String getCallbackAesKey() {
        return callbackAesKey;
    }

    public void setCallbackAesKey(String callbackAesKey) {
        this.callbackAesKey = callbackAesKey;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackOwnerKey() {
        return callbackOwnerKey;
    }

    public void setCallbackOwnerKey(String callbackOwnerKey) {
        this.callbackOwnerKey = callbackOwnerKey;
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }

    public String resolveCallbackOwnerKey() {
        if (callbackOwnerKey != null && !callbackOwnerKey.isBlank()) {
            return callbackOwnerKey;
        }
        if (corpId != null && !corpId.isBlank()) {
            return corpId;
        }
        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }
        return null;
    }
}
