package com.ischool.isp.controller;

import com.ischool.isp.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // Placeholder: 待成员 D 实现 MinIO/本地文件存储
        return Result.error(501, "文件上传功能待实现");
    }

    @GetMapping("/{fileId}")
    public Result<String> download(@PathVariable String fileId) {
        return Result.error(501, "文件下载功能待实现");
    }
}
