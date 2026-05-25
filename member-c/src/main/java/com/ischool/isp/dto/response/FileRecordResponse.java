package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileRecordResponse {

    private Long id;

    private Long ownerId;

    private String relatedType;

    private Long relatedId;

    private String originalName;

    private String fileName;

    private String fileUrl;

    private String previewUrl;

    private String contentType;

    private Long fileSize;

    private String status;

    private LocalDateTime createdAt;
}
