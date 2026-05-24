package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.StatusUpdateRequest;
import com.ischool.isp.dto.request.UserCreateRequest;
import com.ischool.isp.dto.request.UserPasswordResetRequest;
import com.ischool.isp.dto.request.UserUpdateRequest;
import com.ischool.isp.dto.response.UserInfoResponse;
import com.ischool.isp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<PageResult<UserInfoResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        return Result.success(PageResult.of(userService.listUsers(page, pageSize, keyword, role, status)));
    }

    @GetMapping("/{id}")
    public Result<UserInfoResponse> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @OpLog(operationType = "CREATE_USER", description = "创建用户")
    @PostMapping
    public Result<UserInfoResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.createUser(request));
    }

    @OpLog(operationType = "UPDATE_USER", description = "编辑用户")
    @PutMapping("/{id}")
    public Result<UserInfoResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.updateUser(id, request));
    }

    @OpLog(operationType = "RESET_PASSWORD", description = "重置密码")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserPasswordResetRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return Result.success();
    }

    @OpLog(operationType = "UPDATE_USER_STATUS", description = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        userService.updateStatus(id, request.getStatus());
        return Result.success();
    }
}
