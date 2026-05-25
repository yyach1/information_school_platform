package com.ischool.isp.service;

import com.ischool.isp.dto.request.MaterialAuditRequest;
import com.ischool.isp.dto.response.AdminMaterialDetailResponse;
import com.ischool.isp.dto.response.AdminMaterialListItemResponse;

import java.util.List;

public interface MaterialAuditService {

    List<AdminMaterialListItemResponse> listMaterials(Integer page, Integer pageSize, String status, String grade, String className, String processType);

    long countMaterials(String status, String grade, String className, String processType);

    AdminMaterialDetailResponse getMaterialDetail(Long materialId);

    void auditMaterial(Long materialId, MaterialAuditRequest request);
}

