package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank
    private String realName;

    private String phone;
    private String email;

    private String studentNo;
    private String className;
    private String grade;
    private String major;
    private String politicalStatus;

    private String teacherNo;
    private String department;
    private String title;
}
