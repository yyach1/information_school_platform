package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.CertificateAuditRequest;
import com.ischool.isp.dto.request.CertificateIssueRequest;
import com.ischool.isp.dto.response.CertificateResponse;
import com.ischool.isp.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/certificates")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
public class AdminCertificateController {

    private final CertificateService certificateService;

    @GetMapping
    public Result<PageResult<CertificateResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String certType,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String className) {
        return Result.success(PageResult.of(certificateService.adminList(page, pageSize, status, certType, grade, className)));
    }

    @GetMapping("/{id}")
    public Result<CertificateResponse> detail(@PathVariable Long id) {
        return Result.success(certificateService.getDetail(id));
    }

    @OpLog(operationType = "AUDIT_CERTIFICATE", description = "审核电子证明")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody CertificateAuditRequest request) {
        certificateService.audit(id, request);
        return Result.success();
    }

    @OpLog(operationType = "ISSUE_CERTIFICATE", description = "发放电子证明")
    @PostMapping("/{id}/issue")
    public Result<Void> issue(@PathVariable Long id, @Valid @RequestBody CertificateIssueRequest request) {
        certificateService.issue(id, request);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        return Result.success(certificateService.getStats());
    }
}
