package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateListItemResponse {

    private Long id;
    private String certType;
    private String certTypeLabel;
    private String title;
    private String status;
    private String statusLabel;
    private LocalDateTime applyTime;
}
