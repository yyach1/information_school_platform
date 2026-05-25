package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportOverviewResponse {

    private Long studentCount;

    private Long materialCount;

    private Long pendingMaterialCount;

    private Long approvedMaterialCount;

    private Long returnedMaterialCount;

    private Long certificateCount;

    private Long pendingCertificateCount;

    private Long todayUploadCount;

    private Long totalFileSize;
}
