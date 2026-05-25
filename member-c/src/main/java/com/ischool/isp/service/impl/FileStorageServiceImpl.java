package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.config.FileStorageProperties;
import com.ischool.isp.dto.request.FileQueryRequest;
import com.ischool.isp.dto.response.FileRecordResponse;
import com.ischool.isp.entity.FileRecord;
import com.ischool.isp.mapper.FileRecordMapper;
import com.ischool.isp.security.SecurityUtils;
import com.ischool.isp.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileRecordMapper fileRecordMapper;
    private final FileStorageProperties properties;

    @Override
    @Transactional
    public FileRecordResponse upload(MultipartFile file, Long ownerId, String relatedType, Long relatedId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > properties.getMaxSize()) {
            throw new BusinessException("文件大小超过限制");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        Long finalOwnerId = ownerId != null ? ownerId : currentUserId;
        if (finalOwnerId == null) {
            throw new BusinessException(401, "无法识别当前用户");
        }
        if ("STUDENT".equals(role) && !finalOwnerId.equals(currentUserId)) {
            throw new BusinessException(403, "学生只能上传自己的文件");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = getExtension(originalName);
        checkExtension(extension);

        String dateDir = LocalDate.now().toString().replace("-", "");
        String storedName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        Path root = Paths.get(properties.getRootDir()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(dateDir).normalize();
        Path target = targetDir.resolve(storedName).normalize();

        if (!target.startsWith(root)) {
            throw new BusinessException("非法文件路径");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }

        FileRecord record = new FileRecord();
        record.setOwnerId(finalOwnerId);
        record.setRelatedType(StringUtils.hasText(relatedType) ? relatedType.toUpperCase(Locale.ROOT) : "OTHER");
        record.setRelatedId(relatedId);
        record.setOriginalName(originalName);
        record.setStoredName(dateDir + "/" + storedName);
        record.setFileUrl("/api/files/" + 0); // 插入后回填真实 id
        record.setContentType(StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream");
        record.setFileSize(file.getSize());
        record.setStatus("ACTIVE");
        fileRecordMapper.insert(record);

        record.setFileUrl("/api/files/" + record.getId());
        fileRecordMapper.updateById(record);
        return toResponse(record);
    }

    @Override
    public IPage<FileRecordResponse> list(FileQueryRequest request) {
        Integer page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        Integer pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : Math.min(request.getPageSize(), 100);

        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getStatus, "ACTIVE")
                .eq(request.getOwnerId() != null, FileRecord::getOwnerId, request.getOwnerId())
                .eq(StringUtils.hasText(request.getRelatedType()), FileRecord::getRelatedType,
                        request.getRelatedType() == null ? null : request.getRelatedType().toUpperCase(Locale.ROOT))
                .eq(request.getRelatedId() != null, FileRecord::getRelatedId, request.getRelatedId())
                .like(StringUtils.hasText(request.getKeyword()), FileRecord::getOriginalName, request.getKeyword())
                .orderByDesc(FileRecord::getCreatedAt)
                .orderByDesc(FileRecord::getId);

        applyDataScope(wrapper);
        return fileRecordMapper.selectPage(new Page<>(page, pageSize), wrapper).convert(this::toResponse);
    }

    @Override
    public FileRecordResponse getMetadata(Long id) {
        return toResponse(getUsableRecord(id));
    }

    @Override
    public FileRecord getUsableRecord(Long id) {
        FileRecord record = fileRecordMapper.selectById(id);
        if (record == null || !"ACTIVE".equals(record.getStatus())) {
            throw new BusinessException(404, "文件不存在");
        }
        checkRecordPermission(record);
        return record;
    }

    @Override
    public Resource loadAsResource(FileRecord record) {
        try {
            Path root = Paths.get(properties.getRootDir()).toAbsolutePath().normalize();
            Path filePath = root.resolve(record.getStoredName()).normalize();
            if (!filePath.startsWith(root)) {
                throw new BusinessException("非法文件路径");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException(404, "文件内容不存在或不可读");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BusinessException("文件路径解析失败");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FileRecord record = getUsableRecord(id);
        record.setStatus("DELETED");
        fileRecordMapper.updateById(record);
    }

    private void applyDataScope(LambdaQueryWrapper<FileRecord> wrapper) {
        String role = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if ("STUDENT".equals(role)) {
            wrapper.eq(FileRecord::getOwnerId, currentUserId);
        }
    }

    private void checkRecordPermission(FileRecord record) {
        String role = SecurityUtils.getCurrentUserRole();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if ("ADMIN".equals(role) || "TEACHER".equals(role)) {
            return;
        }
        if (!record.getOwnerId().equals(currentUserId)) {
            throw new BusinessException(403, "无权访问该文件");
        }
    }

    private void checkExtension(String extension) {
        Set<String> allowed = properties.getAllowedExtensions().stream()
                .map(ext -> ext.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (!allowed.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("不支持的文件类型：" + extension);
        }
    }

    private String getExtension(String originalName) {
        int idx = originalName.lastIndexOf('.');
        if (idx < 0 || idx == originalName.length() - 1) {
            throw new BusinessException("文件必须包含扩展名");
        }
        return originalName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private FileRecordResponse toResponse(FileRecord record) {
        return FileRecordResponse.builder()
                .id(record.getId())
                .ownerId(record.getOwnerId())
                .relatedType(record.getRelatedType())
                .relatedId(record.getRelatedId())
                .originalName(record.getOriginalName())
                .fileName(record.getStoredName())
                .fileUrl(record.getFileUrl())
                .previewUrl("/api/files/" + record.getId() + "/preview")
                .contentType(record.getContentType())
                .fileSize(record.getFileSize())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
