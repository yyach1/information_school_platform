package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String title;
    private String content;
    private String notificationType;
    private String bizType;
    private Long bizId;
    private String readStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
