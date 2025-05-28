package com.hrg.idolcafeclientapp.data.messages;

import com.hrg.idolcafeclientapp.data.models.SystemConfig;

public class SystemConfigResponse {
    private String Message;
    private SystemConfig Config;

    public SystemConfigResponse(String message, SystemConfig config) {
        Message = message;
        Config = config;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public SystemConfig getConfig() {
        return Config;
    }

    public void setConfig(SystemConfig config) {
        Config = config;
    }
}
