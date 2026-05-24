package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationCreateRequest {

    @NotNull
    private Long receiverId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String notificationType = "NOTICE";

    private String bizType;

    private Long bizId;
}
