package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.MaterialAuditRequest;
import com.ischool.isp.dto.response.AdminMaterialDetailResponse;
import com.ischool.isp.dto.response.AdminMaterialListItemResponse;
import com.ischool.isp.service.MaterialAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/materials")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class AdminMaterialController {

    private final MaterialAuditService materialAuditService;

    @GetMapping
    public Result<PageResult<AdminMaterialListItemResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String processType
    ) {
        List<AdminMaterialListItemResponse> records = materialAuditService.listMaterials(page, pageSize, status, grade, className, processType);
        long total = materialAuditService.countMaterials(status, grade, className, processType);
        long pages = (total + pageSize - 1) / pageSize;
        return Result.success(new PageResult<>(records, total, page.longValue(), pageSize.longValue(), pages));
    }

    @GetMapping("/{id}")
    public Result<AdminMaterialDetailResponse> detail(@PathVariable Long id) {
        return Result.success(materialAuditService.getMaterialDetail(id));
    }

    @OpLog(operationType = "AUDIT_MATERIAL", description = "审核材料")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody MaterialAuditRequest request) {
        materialAuditService.auditMaterial(id, request);
        return Result.success();
    }
}

