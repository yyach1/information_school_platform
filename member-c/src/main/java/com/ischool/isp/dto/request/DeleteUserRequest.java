package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteUserRequest {

    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;
}
