package com.ischool.isp.service;

import com.ischool.isp.dto.request.MaterialSubmitRequest;
import com.ischool.isp.dto.response.NodeRequirementsResponse;
import com.ischool.isp.dto.response.StudentProcessDetailResponse;
import com.ischool.isp.dto.response.TimelineResponse;

public interface StudentProcessService {

    Long getCurrentStudentId();

    Long createOrGetStudentProcess(Long processId);

    StudentProcessDetailResponse getDetail(Long studentProcessId);

    NodeRequirementsResponse getCurrentNodeRequirements(Long studentProcessId);

    Long submitMaterial(Long studentProcessId, MaterialSubmitRequest request);

    TimelineResponse getTimeline(Long studentProcessId);
}

