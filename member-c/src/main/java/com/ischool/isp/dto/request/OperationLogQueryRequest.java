package com.ischool.isp.dto.request;

import lombok.Data;

@Data
public class OperationLogQueryRequest {

    private Long userId;
    private String operationType;
    private String result;
    private String startTime;
    private String endTime;
    private String keyword;
}
