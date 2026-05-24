package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.LoginRequest;
import com.ischool.isp.dto.response.LoginResponse;
import com.ischool.isp.dto.response.UserInfoResponse;
import com.ischool.isp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @OpLog(operationType = "LOGOUT", description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
