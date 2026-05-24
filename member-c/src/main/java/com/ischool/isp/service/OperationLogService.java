package com.ischool.isp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.dto.request.OperationLogQueryRequest;
import com.ischool.isp.dto.response.OperationLogResponse;

public interface OperationLogService {

    IPage<OperationLogResponse> listLogs(Integer page, Integer pageSize, OperationLogQueryRequest query);
}
