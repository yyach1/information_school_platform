package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProcessResponse {

    private Long id;
    private String name;
    private String type;
    private String description;
    private String status;
    private Integer version;
    private LocalDateTime createdAt;
}

