package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CertificateResponse {

    private Long id;
    private String certType;
    private String certTypeLabel;
    private String title;
    private String description;
    private String attachmentUrl;
    private String attachmentName;
    private String status;
    private String statusLabel;
    private String studentName;
    private String studentNo;
    private String className;
    private String approverName;
    private String approveComment;
    private String pdfUrl;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private LocalDateTime issueTime;
    private List<ProgressNode> progressNodes;

    @Data
    @Builder
    public static class ProgressNode {
        private String type;
        private LocalDateTime time;
        private String message;
        private String label;
    }
}
