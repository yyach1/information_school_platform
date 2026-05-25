package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessNodeResponse {

    private Long id;
    private Long processId;
    private String nodeName;
    private Integer nodeOrder;
    private String approverRole;
    private String requiredMaterial;
}

