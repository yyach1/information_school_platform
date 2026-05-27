package com.ischool.isp.controller;

import com.ischool.isp.annotation.OpLog;
import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.FileQueryRequest;
import com.ischool.isp.dto.response.FileRecordResponse;
import com.ischool.isp.entity.FileRecord;
import com.ischool.isp.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @OpLog(operationType = "FILE_UPLOAD", description = "上传文件")
    @PostMapping("/upload")
    public Result<FileRecordResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String relatedType,
            @RequestParam(required = false) Long relatedId,
            @RequestParam(required = false) String originalName) {
        return Result.success(fileStorageService.upload(file, ownerId, relatedType, relatedId, originalName));
    }

    @GetMapping
    public Result<PageResult<FileRecordResponse>> list(FileQueryRequest request) {
        return Result.success(PageResult.of(fileStorageService.list(request)));
    }

    @GetMapping("/{fileId}/meta")
    public Result<FileRecordResponse> metadata(@PathVariable Long fileId) {
        return Result.success(fileStorageService.getMetadata(fileId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        FileRecord record = fileStorageService.getUsableRecord(fileId);
        Resource resource = fileStorageService.loadAsResource(record);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(record.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(record.getOriginalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<Resource> preview(@PathVariable Long fileId) {
        FileRecord record = fileStorageService.getUsableRecord(fileId);
        Resource resource = fileStorageService.loadAsResource(record);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(record.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(record.getOriginalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }

    @OpLog(operationType = "FILE_DELETE", description = "删除文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> delete(@PathVariable Long fileId) {
        fileStorageService.delete(fileId);
        return Result.success();
    }
}
