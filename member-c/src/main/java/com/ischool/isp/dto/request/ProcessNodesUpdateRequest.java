package com.ischool.isp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProcessNodesUpdateRequest {

    @Valid
    @NotEmpty(message = "节点列表不能为空")
    private List<ProcessNodeItem> nodes;

    @Data
    public static class ProcessNodeItem {
        private Long id;
        private String nodeName;
        private Integer nodeOrder;
        private String approverRole;
        private String requiredMaterial;
    }
}

