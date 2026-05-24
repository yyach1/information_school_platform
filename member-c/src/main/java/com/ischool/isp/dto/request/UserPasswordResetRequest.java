package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserPasswordResetRequest {

    @NotBlank @Size(min = 6, max = 64)
    private String newPassword;
}
