package com.ischool.isp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.dto.request.StudentProgressQueryRequest;
import com.ischool.isp.dto.response.StudentProgressResponse;

public interface StudentProgressService {

    IPage<StudentProgressResponse> getStudentProgress(Integer page, Integer pageSize, StudentProgressQueryRequest query);
}
