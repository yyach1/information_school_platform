package com.ischool.isp.service;

import com.ischool.isp.dto.request.LoginRequest;
import com.ischool.isp.dto.response.LoginResponse;
import com.ischool.isp.dto.response.UserInfoResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserInfoResponse getCurrentUser();

    void updateAvatar(String avatarUrl);

    void changePassword(String oldPassword, String newPassword);

    void logout();
}
