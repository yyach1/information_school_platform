package com.ischool.isp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long receiverId;

    private String title;

    private String content;

    private String notificationType;

    private String bizType;

    private Long bizId;

    private String readStatus;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
