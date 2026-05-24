package com.ischool.isp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String operationType;

    private String operationContent;

    private String bizType;

    private Long bizId;

    private String result;

    private String failReason;

    private String ipAddress;

    private String userAgent;

    private String requestId;

    private LocalDateTime operationTime;
}
