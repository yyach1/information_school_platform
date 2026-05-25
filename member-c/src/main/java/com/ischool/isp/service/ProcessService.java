package com.ischool.isp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.dto.request.ProcessCreateRequest;
import com.ischool.isp.dto.request.ProcessNodesUpdateRequest;
import com.ischool.isp.dto.request.ProcessUpdateRequest;
import com.ischool.isp.dto.response.ProcessNodeResponse;
import com.ischool.isp.dto.response.ProcessResponse;

import java.util.List;

public interface ProcessService {

    IPage<ProcessResponse> listProcesses(Integer page, Integer pageSize, String keyword, String status);

    List<ProcessResponse> listEnabledProcesses(String status);

    ProcessResponse create(ProcessCreateRequest request);

    ProcessResponse update(Long id, ProcessUpdateRequest request);

    List<ProcessNodeResponse> listNodes(Long processId);

    void updateNodes(Long processId, ProcessNodesUpdateRequest request);
}

