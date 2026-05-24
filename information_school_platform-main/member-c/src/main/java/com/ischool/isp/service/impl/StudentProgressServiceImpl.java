package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ischool.isp.dto.request.StudentProgressQueryRequest;
import com.ischool.isp.dto.response.StudentProgressResponse;
import com.ischool.isp.entity.Student;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.StudentMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.service.StudentProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProgressServiceImpl implements StudentProgressService {

    private final StudentMapper studentMapper;
    private final UserMapper userMapper;

    @Override
    public IPage<StudentProgressResponse> getStudentProgress(Integer page, Integer pageSize, StudentProgressQueryRequest query) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(StringUtils.hasText(query.getGrade()), Student::getGrade, query.getGrade())
                .eq(StringUtils.hasText(query.getClassName()), Student::getClassName, query.getClassName())
                .eq(StringUtils.hasText(query.getPoliticalStatus()), Student::getPoliticalStatus, query.getPoliticalStatus())
                .orderByAsc(Student::getGrade, Student::getClassName, Student::getStudentNo);

        Page<Student> studentPage = new Page<>(page, pageSize);
        IPage<Student> result = studentMapper.selectPage(studentPage, wrapper);

        // batch load user info
        List<Long> userIds = result.getRecords().stream()
                .map(Student::getUserId)
                .collect(Collectors.toList());
        Map<Long, User> userMap = Map.of();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        // keyword filter on username/realName (post-filter since it crosses tables)
        String keyword = query.getKeyword();
        Map<Long, User> finalUserMap = userMap;
        return result.convert(student -> {
            User user = finalUserMap.get(student.getUserId());
            if (user == null) return null;
            if (StringUtils.hasText(keyword)) {
                boolean matches = (user.getUsername() != null && user.getUsername().contains(keyword))
                        || (user.getRealName() != null && user.getRealName().contains(keyword));
                if (!matches) return null;
            }
            return StudentProgressResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .studentNo(student.getStudentNo())
                    .className(student.getClassName())
                    .grade(student.getGrade())
                    .major(student.getMajor())
                    .politicalStatus(student.getPoliticalStatus())
                    .status(user.getStatus())
                    .build();
        });
    }
}
