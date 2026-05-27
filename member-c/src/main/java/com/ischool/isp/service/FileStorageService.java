package com.ischool.isp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.dto.request.FileQueryRequest;
import com.ischool.isp.dto.response.FileRecordResponse;
import com.ischool.isp.entity.FileRecord;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileRecordResponse upload(MultipartFile file, Long ownerId, String relatedType, Long relatedId, String originalName);

    IPage<FileRecordResponse> list(FileQueryRequest request);

    FileRecordResponse getMetadata(Long id);

    FileRecord getUsableRecord(Long id);

    Resource loadAsResource(FileRecord record);

    void delete(Long id);
}
