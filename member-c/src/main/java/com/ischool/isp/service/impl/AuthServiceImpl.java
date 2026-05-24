package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.request.LoginRequest;
import com.ischool.isp.dto.response.LoginResponse;
import com.ischool.isp.dto.response.UserInfoResponse;
import com.ischool.isp.entity.Student;
import com.ischool.isp.entity.Teacher;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.StudentMapper;
import com.ischool.isp.mapper.TeacherMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.security.JwtTokenProvider;
import com.ischool.isp.security.SecurityUtils;
import com.ischool.isp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private long expirationMs;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("账户已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expirationMs)
                .userInfo(buildUserInfo(user))
                .build();
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return buildUserInfo(user);
    }

    @Override
    public void logout() {
        // Stateless JWT — client discards token. No server-side invalidation needed.
    }

    private UserInfoResponse buildUserInfo(User user) {
        UserInfoResponse.UserInfoResponseBuilder builder = UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .phone(user.getPhone())
                .email(user.getEmail())
                .status(user.getStatus());

        if ("STUDENT".equals(user.getRole())) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, user.getId())
            );
            if (student != null) {
                builder.studentNo(student.getStudentNo())
                        .className(student.getClassName())
                        .grade(student.getGrade())
                        .major(student.getMajor())
                        .politicalStatus(student.getPoliticalStatus());
            }
        } else if ("TEACHER".equals(user.getRole())) {
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, user.getId())
            );
            if (teacher != null) {
                builder.teacherNo(teacher.getTeacherNo())
                        .department(teacher.getDepartment())
                        .title(teacher.getTitle());
            }
        }

        return builder.build();
    }
}
