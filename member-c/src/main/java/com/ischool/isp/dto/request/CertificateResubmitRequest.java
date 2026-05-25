package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertificateResubmitRequest {

    @NotBlank(message = "title不能为空")
    private String title;

    private String description;
    private String attachmentUrl;
    private String attachmentName;
}
