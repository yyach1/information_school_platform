package com.ischool.isp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.dto.request.NotificationCreateRequest;
import com.ischool.isp.dto.response.NotificationResponse;
import com.ischool.isp.dto.response.UnreadCountResponse;

public interface NotificationService {

    IPage<NotificationResponse> listNotifications(Integer page, Integer pageSize, String type, String readStatus);

    UnreadCountResponse getUnreadCount();

    void markAsRead(Long id);

    void createNotification(NotificationCreateRequest request);
}
