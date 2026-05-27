package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.request.UserCreateRequest;
import com.ischool.isp.dto.request.UserUpdateRequest;
import com.ischool.isp.dto.response.UserInfoResponse;
import com.ischool.isp.entity.Student;
import com.ischool.isp.entity.Teacher;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.StudentMapper;
import com.ischool.isp.mapper.TeacherMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.security.SecurityUtils;
import com.ischool.isp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserInfoResponse> listUsers(Integer page, Integer pageSize, String keyword, String role, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .or()
                .like(StringUtils.hasText(keyword), User::getRealName, keyword)
                .eq(StringUtils.hasText(role), User::getRole, role)
                .eq(StringUtils.hasText(status), User::getStatus, status)
                .orderByDesc(User::getCreatedAt);

        // Handle the keyword OR condition properly
        if (StringUtils.hasText(keyword)) {
            wrapper = new LambdaQueryWrapper<User>()
                    .and(w -> w.like(User::getUsername, keyword).or().like(User::getRealName, keyword))
                    .eq(StringUtils.hasText(role), User::getRole, role)
                    .eq(StringUtils.hasText(status), User::getStatus, status)
                    .orderByDesc(User::getCreatedAt);
        }

        Page<User> userPage = new Page<>(page, pageSize);
        IPage<User> result = userMapper.selectPage(userPage, wrapper);

        return result.convert(this::toUserInfo);
    }

    @Override
    public UserInfoResponse getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return toUserInfo(user);
    }

    @Override
    @Transactional
    public UserInfoResponse createUser(UserCreateRequest request) {
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException("用户名已存在");
        }
        if ("STUDENT".equals(request.getRole()) && request.getStudentNo() != null
                && studentMapper.exists(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, request.getStudentNo()))) {
            throw new BusinessException("学号已存在");
        }
        if ("TEACHER".equals(request.getRole()) && request.getTeacherNo() != null
                && teacherMapper.exists(new LambdaQueryWrapper<Teacher>().eq(Teacher::getTeacherNo, request.getTeacherNo()))) {
            throw new BusinessException("工号已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        if ("STUDENT".equals(request.getRole())) {
            Student student = new Student();
            student.setUserId(user.getId());
            student.setStudentNo(request.getStudentNo() != null ? request.getStudentNo() : request.getUsername());
            student.setClassName(request.getClassName());
            student.setGrade(request.getGrade());
            student.setMajor(request.getMajor());
            student.setPoliticalStatus(request.getPoliticalStatus());
            studentMapper.insert(student);
        } else if ("TEACHER".equals(request.getRole())) {
            Teacher teacher = new Teacher();
            teacher.setUserId(user.getId());
            teacher.setTeacherNo(request.getTeacherNo() != null ? request.getTeacherNo() : request.getUsername());
            teacher.setDepartment(request.getDepartment());
            teacher.setTitle(request.getTitle());
            teacherMapper.insert(teacher);
        }

        return toUserInfo(user);
    }

    @Override
    @Transactional
    public UserInfoResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        userMapper.updateById(user);

        if ("STUDENT".equals(user.getRole())) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, id)
            );
            if (student != null) {
                if (request.getStudentNo() != null) {
                    student.setStudentNo(request.getStudentNo());
                }
                student.setClassName(request.getClassName());
                student.setGrade(request.getGrade());
                student.setMajor(request.getMajor());
                student.setPoliticalStatus(request.getPoliticalStatus());
                studentMapper.updateById(student);
            }
        } else if ("TEACHER".equals(user.getRole())) {
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, id)
            );
            if (teacher != null) {
                if (request.getTeacherNo() != null) {
                    teacher.setTeacherNo(request.getTeacherNo());
                }
                teacher.setDepartment(request.getDepartment());
                teacher.setTitle(request.getTitle());
                teacherMapper.updateById(teacher);
            }
        }

        return toUserInfo(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void updateStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
            throw new BusinessException("状态值无效");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String adminPassword) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "未登录");
        }
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            throw new BusinessException(403, "仅管理员可删除用户");
        }
        if (!passwordEncoder.matches(adminPassword, currentUser.getPasswordHash())) {
            throw new BusinessException("管理员密码错误");
        }
        User target = userMapper.selectById(id);
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (currentUserId.equals(id)) {
            throw new BusinessException("不能删除自己的账号");
        }
        studentMapper.delete(new LambdaQueryWrapper<Student>().eq(Student::getUserId, id));
        teacherMapper.delete(new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, id));
        userMapper.deleteById(id);
    }

    private UserInfoResponse toUserInfo(User user) {
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
