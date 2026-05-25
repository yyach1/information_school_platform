package com.ischool.isp.controller;

import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.MaterialSubmitRequest;
import com.ischool.isp.dto.request.StudentProcessCreateRequest;
import com.ischool.isp.dto.response.NodeRequirementsResponse;
import com.ischool.isp.dto.response.ProcessResponse;
import com.ischool.isp.dto.response.StudentProcessDetailResponse;
import com.ischool.isp.dto.response.TimelineResponse;
import com.ischool.isp.service.ProcessService;
import com.ischool.isp.service.StudentProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentWorkflowController {

    private final ProcessService processService;
    private final StudentProcessService studentProcessService;

    @GetMapping("/processes")
    public Result<Map<String, Object>> listProcesses(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        List<ProcessResponse> items = processService.listEnabledProcesses(status);
        return Result.success(Map.of(
                "items", items,
                "page", page,
                "size", size,
                "total", items.size()
        ));
    }

    @PostMapping("/student-processes")
    public Result<Map<String, Object>> createOrGetStudentProcess(@Valid @RequestBody StudentProcessCreateRequest request) {
        Long id = studentProcessService.createOrGetStudentProcess(request.getProcessId());
        StudentProcessDetailResponse detail = studentProcessService.getDetail(id);
        return Result.success(Map.of(
                "id", id,
                "status", detail.getStatus(),
                "currentNodeId", detail.getCurrentNode() != null ? detail.getCurrentNode().getId() : null
        ));
    }

    @GetMapping("/student-processes/{id}")
    public Result<StudentProcessDetailResponse> detail(@PathVariable Long id) {
        return Result.success(studentProcessService.getDetail(id));
    }

    @GetMapping("/student-processes/{id}/current-node/requirements")
    public Result<NodeRequirementsResponse> requirements(@PathVariable Long id) {
        return Result.success(studentProcessService.getCurrentNodeRequirements(id));
    }

    @PostMapping("/student-processes/{id}/materials")
    public Result<Map<String, Object>> submitMaterial(@PathVariable Long id, @Valid @RequestBody MaterialSubmitRequest request) {
        Long materialId = studentProcessService.submitMaterial(id, request);
        return Result.success(Map.of("materialId", materialId, "status", "PENDING"));
    }

    @GetMapping("/student-processes/{id}/timeline")
    public Result<TimelineResponse> timeline(@PathVariable Long id) {
        return Result.success(studentProcessService.getTimeline(id));
    }
}

