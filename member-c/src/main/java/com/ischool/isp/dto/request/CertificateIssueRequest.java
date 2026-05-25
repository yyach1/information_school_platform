package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertificateIssueRequest {

    @NotBlank(message = "pdfUrl不能为空")
    private String pdfUrl;
}
