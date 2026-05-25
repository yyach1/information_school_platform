package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentProcessCreateRequest {

    @NotNull(message = "processId不能为空")
    private Long processId;
}

