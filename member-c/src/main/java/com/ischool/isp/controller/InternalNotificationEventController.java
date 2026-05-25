package com.ischool.isp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.NotificationCreateRequest;
import com.ischool.isp.dto.request.NotificationEventRequest;
import com.ischool.isp.entity.Student;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.StudentMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/notification-events")
@RequiredArgsConstructor
public class InternalNotificationEventController {

    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;

    @PostMapping
    public Result<Void> accept(@Valid @RequestBody NotificationEventRequest request) {
        Map<String, Object> payload = request.getPayload();
        if (payload == null) {
            return Result.success();
        }

        switch (request.getEventType()) {
            case "MATERIAL_SUBMITTED" -> {
                String title = payload.get("title") != null ? payload.get("title").toString() : "材料待审核";
                String content = payload.get("content") != null ? payload.get("content").toString() : "有新的材料待审核";
                Long bizId = asLong(payload.get("materialId"));
                createTodoToTeachers(title, content, request.getEventType(), bizId);
            }
            case "MATERIAL_AUDITED" -> {
                Long studentId = asLong(payload.get("studentId"));
                Long materialId = asLong(payload.get("materialId"));
                String title = payload.get("title") != null ? payload.get("title").toString() : "材料审核结果";
                String content = payload.get("content") != null ? payload.get("content").toString() : "材料状态已更新";
                createNoticeToStudent(studentId, title, content, request.getEventType(), materialId);
            }
            default -> {
            }
        }
        return Result.success();
    }

    private void createTodoToTeachers(String title, String content, String bizType, Long bizId) {
        List<User> receivers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "TEACHER"));
        if (receivers.isEmpty()) {
            receivers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
        }
        for (User u : receivers) {
            NotificationCreateRequest req = new NotificationCreateRequest();
            req.setReceiverId(u.getId());
            req.setNotificationType("TODO");
            req.setTitle(title);
            req.setContent(content);
            req.setBizType(bizType);
            req.setBizId(bizId);
            notificationService.createNotification(req);
        }
    }

    private void createNoticeToStudent(Long studentId, String title, String content, String bizType, Long bizId) {
        if (studentId == null) return;
        Student student = studentMapper.selectById(studentId);
        if (student == null) return;
        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setReceiverId(student.getUserId());
        req.setNotificationType("NOTICE");
        req.setTitle(title);
        req.setContent(content);
        req.setBizType(bizType);
        req.setBizId(bizId);
        notificationService.createNotification(req);
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
}

