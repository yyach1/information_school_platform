package com.ischool.isp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "isp.file-storage")
public class FileStorageProperties {

    /**
     * 本地文件存储根目录。课程项目阶段默认使用本地 uploads 目录，后续可替换为对象存储。
     */
    private String rootDir = "uploads";

    /**
     * 单文件最大字节数，默认 20MB。
     */
    private long maxSize = 20 * 1024 * 1024L;

    /**
     * 允许上传的文件扩展名。
     */
    private List<String> allowedExtensions = new ArrayList<>(List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "png", "jpg", "jpeg", "txt"
    ));
}
