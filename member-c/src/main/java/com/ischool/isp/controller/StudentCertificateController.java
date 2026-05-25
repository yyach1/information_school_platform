package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.CertificateApplyRequest;
import com.ischool.isp.dto.request.CertificateResubmitRequest;
import com.ischool.isp.dto.response.CertificateListItemResponse;
import com.ischool.isp.dto.response.CertificateResponse;
import com.ischool.isp.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student/certificates")
@RequiredArgsConstructor
public class StudentCertificateController {

    private final CertificateService certificateService;

    @GetMapping("/types")
    public Result<Map<String, Object>> getTypes() {
        return Result.success(Map.of("items", certificateService.getCertificateTypes()));
    }

    @OpLog(operationType = "APPLY_CERTIFICATE", description = "申请电子证明")
    @PostMapping
    public Result<CertificateResponse> apply(@Valid @RequestBody CertificateApplyRequest request) {
        return Result.success(certificateService.apply(request));
    }

    @GetMapping
    public Result<PageResult<CertificateListItemResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String certType) {
        return Result.success(PageResult.of(certificateService.listMyCertificates(page, pageSize, status, certType)));
    }

    @GetMapping("/{id}")
    public Result<CertificateResponse> detail(@PathVariable Long id) {
        return Result.success(certificateService.getDetail(id));
    }

    @OpLog(operationType = "RESUBMIT_CERTIFICATE", description = "重新提交证明申请")
    @PutMapping("/{id}")
    public Result<CertificateResponse> resubmit(@PathVariable Long id, @Valid @RequestBody CertificateResubmitRequest request) {
        return Result.success(certificateService.resubmit(id, request));
    }

    @GetMapping("/{id}/download")
    public Result<Map<String, String>> download(@PathVariable Long id) {
        CertificateResponse cert = certificateService.downloadPdfUrl(id);
        return Result.success(Map.of("pdfUrl", cert.getPdfUrl()));
    }
}
