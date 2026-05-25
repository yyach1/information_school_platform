package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationEventRequest {

    @NotBlank(message = "eventType不能为空")
    private String eventType;

    private LocalDateTime occurredAt;

    @NotBlank(message = "bizKey不能为空")
    private String bizKey;

    private Map<String, Object> payload;
}

