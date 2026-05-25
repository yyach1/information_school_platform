package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessCreateRequest {

    @NotBlank(message = "流程名称不能为空")
    private String name;

    @NotBlank(message = "流程类型不能为空")
    private String type;

    private String description;
}

