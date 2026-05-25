package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NodeRequirementsResponse {

    private Long nodeId;
    private String nodeName;
    private List<RequiredMaterial> requiredMaterial;

    @Data
    @Builder
    public static class RequiredMaterial {
        private String materialType;
        private Boolean required;
        private Integer maxSizeMB;
        private List<String> ext;
    }
}

