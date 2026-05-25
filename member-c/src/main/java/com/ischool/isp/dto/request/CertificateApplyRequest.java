package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertificateApplyRequest {

    @NotBlank(message = "certType不能为空")
    private String certType;

    @NotBlank(message = "title不能为空")
    private String title;

    private String description;
    private String attachmentUrl;
    private String attachmentName;
}
