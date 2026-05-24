package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.request.NotificationCreateRequest;
import com.ischool.isp.dto.response.NotificationResponse;
import com.ischool.isp.dto.response.UnreadCountResponse;
import com.ischool.isp.entity.Notification;
import com.ischool.isp.mapper.NotificationMapper;
import com.ischool.isp.security.SecurityUtils;
import com.ischool.isp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public IPage<NotificationResponse> listNotifications(Integer page, Integer pageSize, String type, String readStatus) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(StringUtils.hasText(type), Notification::getNotificationType, type)
                .eq(StringUtils.hasText(readStatus), Notification::getReadStatus, readStatus)
                .orderByDesc(Notification::getCreatedAt);

        // Data scoping: STUDENT only sees own notifications
        if ("STUDENT".equals(role) || "TEACHER".equals(role)) {
            wrapper.eq(Notification::getReceiverId, currentUserId);
        }
        // ADMIN sees all — no additional filter

        Page<Notification> notificationPage = new Page<>(page, pageSize);
        IPage<Notification> result = notificationMapper.selectPage(notificationPage, wrapper);

        return result.convert(this::toResponse);
    }

    @Override
    public UnreadCountResponse getUnreadCount() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Long noticeCount = notificationMapper.countUnreadByType(currentUserId, "NOTICE");
        Long todoCount = notificationMapper.countUnreadByType(currentUserId, "TODO");
        return UnreadCountResponse.builder()
                .noticeCount(noticeCount)
                .todoCount(todoCount)
                .totalCount(noticeCount + todoCount)
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();

        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }

        // Only ADMIN can mark others' notifications; STUDENT/TEACHER can only mark their own
        if (!"ADMIN".equals(role) && !notification.getReceiverId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作此通知");
        }

        if ("UNREAD".equals(notification.getReadStatus())) {
            notification.setReadStatus("READ");
            notification.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public void createNotification(NotificationCreateRequest request) {
        Notification notification = new Notification();
        notification.setReceiverId(request.getReceiverId());
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setNotificationType(
                request.getNotificationType() != null ? request.getNotificationType() : "NOTICE"
        );
        notification.setBizType(request.getBizType());
        notification.setBizId(request.getBizId());
        notification.setReadStatus("UNREAD");
        notificationMapper.insert(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .notificationType(n.getNotificationType())
                .bizType(n.getBizType())
                .bizId(n.getBizId())
                .readStatus(n.getReadStatus())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}
