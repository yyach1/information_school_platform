package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentProgressResponse {

    private Long userId;
    private String username;
    private String realName;
    private String studentNo;
    private String className;
    private String grade;
    private String major;
    private String politicalStatus;
    private String status;
}
