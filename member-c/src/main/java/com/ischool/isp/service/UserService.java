package com.ischool.isp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.dto.request.UserCreateRequest;
import com.ischool.isp.dto.request.UserUpdateRequest;
import com.ischool.isp.dto.response.UserInfoResponse;

public interface UserService {

    IPage<UserInfoResponse> listUsers(Integer page, Integer pageSize, String keyword, String role, String status);

    UserInfoResponse getUserById(Long id);

    UserInfoResponse createUser(UserCreateRequest request);

    UserInfoResponse updateUser(Long id, UserUpdateRequest request);

    void resetPassword(Long id, String newPassword);

    void updateStatus(Long id, String status);
}
