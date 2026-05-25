package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessUpdateRequest {

    @NotBlank(message = "流程名称不能为空")
    private String name;

    private String description;

    @NotBlank(message = "流程状态不能为空")
    private String status;
}

