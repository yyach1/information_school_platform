package com.ischool.isp.service;

import com.ischool.isp.dto.response.ReportCountItem;
import com.ischool.isp.dto.response.ReportOverviewResponse;
import com.ischool.isp.dto.response.UploadTrendItem;

import java.util.List;

public interface ReportService {

    ReportOverviewResponse overview();

    List<ReportCountItem> materialStatus();

    List<ReportCountItem> processStatus();

    List<ReportCountItem> certificateStatus();

    List<UploadTrendItem> uploadTrend(Integer days);

    byte[] exportCsv(String type);
}
