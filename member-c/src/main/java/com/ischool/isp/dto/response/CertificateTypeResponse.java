package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificateTypeResponse {

    private String certType;
    private String label;
    private String description;
    private Boolean requireAttachment;
}
