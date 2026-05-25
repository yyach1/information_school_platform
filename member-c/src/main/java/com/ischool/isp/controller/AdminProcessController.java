package com.ischool.isp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.ProcessCreateRequest;
import com.ischool.isp.dto.request.ProcessNodesUpdateRequest;
import com.ischool.isp.dto.request.ProcessUpdateRequest;
import com.ischool.isp.dto.response.ProcessNodeResponse;
import com.ischool.isp.dto.response.ProcessResponse;
import com.ischool.isp.service.ProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/processes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class AdminProcessController {

    private final ProcessService processService;

    @GetMapping
    public Result<PageResult<ProcessResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        IPage<ProcessResponse> result = processService.listProcesses(page, pageSize, keyword, status);
        return Result.success(PageResult.of(result));
    }

    @OpLog(operationType = "CREATE_PROCESS", description = "创建流程模板")
    @PostMapping
    public Result<ProcessResponse> create(@Valid @RequestBody ProcessCreateRequest request) {
        return Result.success(processService.create(request));
    }

    @OpLog(operationType = "UPDATE_PROCESS", description = "更新流程模板")
    @PutMapping("/{id}")
    public Result<ProcessResponse> update(@PathVariable Long id, @Valid @RequestBody ProcessUpdateRequest request) {
        return Result.success(processService.update(id, request));
    }

    @OpLog(operationType = "UPDATE_PROCESS_NODES", description = "配置流程节点")
    @PutMapping("/{id}/nodes")
    public Result<Void> updateNodes(@PathVariable Long id, @Valid @RequestBody ProcessNodesUpdateRequest request) {
        processService.updateNodes(id, request);
        return Result.success();
    }

    @GetMapping("/{id}/nodes")
    public Result<List<ProcessNodeResponse>> listNodes(@PathVariable Long id) {
        return Result.success(processService.listNodes(id));
    }
}
