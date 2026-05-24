package com.ischool.isp.aspect;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.entity.OperationLog;
import com.ischool.isp.mapper.OperationLogMapper;
import com.ischool.isp.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OpLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final HttpServletRequest request;

    @Around("@annotation(opLog)")
    public Object recordLog(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        OperationLog entity = new OperationLog();
        Long userId = SecurityUtils.getCurrentUserId();
        entity.setUserId(userId);
        entity.setOperationType(opLog.operationType());
        entity.setOperationContent(opLog.description());
        entity.setIpAddress(getClientIp());
        entity.setUserAgent(request.getHeader("User-Agent"));
        entity.setOperationTime(LocalDateTime.now());

        try {
            Object result = pjp.proceed();
            entity.setResult("SUCCESS");
            return result;
        } catch (Throwable t) {
            entity.setResult("FAIL");
            String reason = t.getMessage();
            entity.setFailReason(reason != null && reason.length() > 255 ? reason.substring(0, 255) : reason);
            throw t;
        } finally {
            CompletableFuture.runAsync(() -> {
                try {
                    operationLogMapper.insert(entity);
                } catch (Exception e) {
                    log.error("操作日志写入失败", e);
                }
            });
        }
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
