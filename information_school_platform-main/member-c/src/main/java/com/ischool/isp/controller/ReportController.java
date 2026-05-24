package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.response.ReportCountItem;
import com.ischool.isp.dto.response.ReportOverviewResponse;
import com.ischool.isp.dto.response.UploadTrendItem;
import com.ischool.isp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public Result<ReportOverviewResponse> overview() {
        return Result.success(reportService.overview());
    }

    @GetMapping("/material-status")
    public Result<List<ReportCountItem>> materialStatus() {
        return Result.success(reportService.materialStatus());
    }

    @GetMapping("/process-status")
    public Result<List<ReportCountItem>> processStatus() {
        return Result.success(reportService.processStatus());
    }

    @GetMapping("/certificate-status")
    public Result<List<ReportCountItem>> certificateStatus() {
        return Result.success(reportService.certificateStatus());
    }

    @GetMapping("/upload-trend")
    public Result<List<UploadTrendItem>> uploadTrend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(reportService.uploadTrend(days));
    }

    @OpLog(operationType = "REPORT_EXPORT", description = "导出统计报表")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "overview") String type) {
        byte[] content = reportService.exportCsv(type);
        String filename = "report-" + type + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(content);
    }
}
