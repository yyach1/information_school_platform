package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OperationLogResponse {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String operationType;
    private String operationContent;
    private String bizType;
    private Long bizId;
    private String result;
    private String failReason;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private LocalDateTime operationTime;
}
