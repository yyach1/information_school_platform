package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {

    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String phone;
    private String email;
    private String status;

    private String studentNo;
    private String className;
    private String grade;
    private String major;
    private String politicalStatus;

    private String avatarUrl;

    private String teacherNo;
    private String department;
    private String title;
}
