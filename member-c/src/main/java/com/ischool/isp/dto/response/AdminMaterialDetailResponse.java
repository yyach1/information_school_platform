package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminMaterialDetailResponse {

    private Long id;
    private Long studentProcessId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String grade;
    private String className;
    private String processType;
    private String processName;
    private String nodeName;
    private String materialType;
    private String fileUrl;
    private String fileName;
    private String description;
    private String status;
    private LocalDateTime submitTime;
    private List<ApprovalRecordResponse> approvals;

    @Data
    @Builder
    public static class ApprovalRecordResponse {
        private Long id;
        private Long approverId;
        private String approverName;
        private String result;
        private String comment;
        private LocalDateTime approveTime;
    }
}

