package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.AvatarUpdateRequest;
import com.ischool.isp.dto.request.ChangePasswordRequest;
import com.ischool.isp.dto.request.LoginRequest;
import com.ischool.isp.dto.response.LoginResponse;
import com.ischool.isp.dto.response.UserInfoResponse;
import com.ischool.isp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @OpLog(operationType = "LOGIN", description = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> me() {
        return Result.success(authService.getCurrentUser());
    }

    @OpLog(operationType = "UPDATE_AVATAR", description = "更新头像")
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@Valid @RequestBody AvatarUpdateRequest request) {
        authService.updateAvatar(request.getAvatarUrl());
        return Result.success();
    }

    @OpLog(operationType = "CHANGE_PASSWORD", description = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @OpLog(operationType = "LOGOUT", description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
