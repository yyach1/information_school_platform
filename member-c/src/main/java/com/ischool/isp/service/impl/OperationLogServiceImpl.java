package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ischool.isp.dto.request.OperationLogQueryRequest;
import com.ischool.isp.dto.response.OperationLogResponse;
import com.ischool.isp.entity.OperationLog;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.OperationLogMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<OperationLogResponse> listLogs(Integer page, Integer pageSize, OperationLogQueryRequest query) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .eq(query.getUserId() != null, OperationLog::getUserId, query.getUserId())
                .eq(StringUtils.hasText(query.getOperationType()), OperationLog::getOperationType, query.getOperationType())
                .eq(StringUtils.hasText(query.getResult()), OperationLog::getResult, query.getResult())
                .like(StringUtils.hasText(query.getKeyword()), OperationLog::getOperationContent, query.getKeyword())
                .orderByDesc(OperationLog::getOperationTime);

        if (StringUtils.hasText(query.getStartTime())) {
            try {
                wrapper.ge(OperationLog::getOperationTime, LocalDateTime.parse(query.getStartTime(), FORMATTER));
            } catch (Exception ignored) {
            }
        }
        if (StringUtils.hasText(query.getEndTime())) {
            try {
                wrapper.le(OperationLog::getOperationTime, LocalDateTime.parse(query.getEndTime(), FORMATTER));
            } catch (Exception ignored) {
            }
        }

        Page<OperationLog> logPage = new Page<>(page, pageSize);
        IPage<OperationLog> result = operationLogMapper.selectPage(logPage, wrapper);

        return result.convert(this::toResponse);
    }

    private OperationLogResponse toResponse(OperationLog log) {
        String username = null;
        String realName = null;
        if (log.getUserId() != null) {
            User user = userMapper.selectById(log.getUserId());
            if (user != null) {
                username = user.getUsername();
                realName = user.getRealName();
            }
        }

        return OperationLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(username)
                .realName(realName)
                .operationType(log.getOperationType())
                .operationContent(log.getOperationContent())
                .bizType(log.getBizType())
                .bizId(log.getBizId())
                .result(log.getResult())
                .failReason(log.getFailReason())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .requestId(log.getRequestId())
                .operationTime(log.getOperationTime())
                .build();
    }
}
