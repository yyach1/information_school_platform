package com.ischool.isp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarUpdateRequest {

    @NotBlank(message = "avatarUrl不能为空")
    private String avatarUrl;
}
