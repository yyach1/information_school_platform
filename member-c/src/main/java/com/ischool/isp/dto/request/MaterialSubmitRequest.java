package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialSubmitRequest {

    @NotNull(message = "nodeId不能为空")
    private Long nodeId;

    @NotBlank(message = "materialType不能为空")
    private String materialType;

    @NotBlank(message = "fileUrl不能为空")
    private String fileUrl;

    @NotBlank(message = "fileName不能为空")
    private String fileName;

    private String description;
}

