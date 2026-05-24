package com.ischool.isp.controller;

import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.OperationLogQueryRequest;
import com.ischool.isp.dto.response.OperationLogResponse;
import com.ischool.isp.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public Result<PageResult<OperationLogResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String keyword) {
        OperationLogQueryRequest query = new OperationLogQueryRequest();
        query.setUserId(userId);
        query.setOperationType(operationType);
        query.setResult(result);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setKeyword(keyword);
        return Result.success(PageResult.of(operationLogService.listLogs(page, pageSize, query)));
    }
}
