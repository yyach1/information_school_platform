package com.ischool.isp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank @Size(min = 3, max = 64)
    private String username;

    @NotBlank @Size(min = 6, max = 64)
    private String password;

    @NotBlank
    private String realName;

    @NotBlank
    private String role;

    private String phone;

    @Email
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
