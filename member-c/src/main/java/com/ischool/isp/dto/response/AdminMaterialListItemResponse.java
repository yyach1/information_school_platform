package com.ischool.isp.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMaterialListItemResponse {

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
    private String status;
    private LocalDateTime submitTime;
}

