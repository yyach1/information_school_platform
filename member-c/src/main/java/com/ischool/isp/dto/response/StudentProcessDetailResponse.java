package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudentProcessDetailResponse {

    private Long id;
    private ProcessSummary process;
    private String status;
    private NodeSummary currentNode;
    private List<NodeStatus> nodes;
    private List<MaterialItem> materials;
    private LocalDateTime startTime;
    private LocalDateTime updateTime;

    @Data
    @Builder
    public static class ProcessSummary {
        private Long id;
        private String name;
        private String type;
        private Integer version;
    }

    @Data
    @Builder
    public static class NodeSummary {
        private Long id;
        private String nodeName;
        private Integer nodeOrder;
        private String approverRole;
    }

    @Data
    @Builder
    public static class NodeStatus {
        private Long id;
        private String nodeName;
        private Integer nodeOrder;
        private String status;
    }

    @Data
    @Builder
    public static class MaterialItem {
        private Long id;
        private Long nodeId;
        private String materialType;
        private String fileUrl;
        private String fileName;
        private String status;
        private LocalDateTime submitTime;
    }
}

