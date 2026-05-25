package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertificateAuditRequest {

    @NotBlank(message = "result不能为空")
    private String result;

    private String comment;
}
