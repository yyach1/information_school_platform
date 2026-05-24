package com.ischool.isp.controller;

import com.ischool.isp.common.PageResult;
import com.ischool.isp.common.Result;
import com.ischool.isp.dto.request.StudentProgressQueryRequest;
import com.ischool.isp.dto.response.StudentProgressResponse;
import com.ischool.isp.service.StudentProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/students/progress")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class StudentProgressController {

    private final StudentProgressService studentProgressService;

    @GetMapping
    public Result<PageResult<StudentProgressResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String politicalStatus,
            @RequestParam(required = false) String keyword) {
        StudentProgressQueryRequest query = new StudentProgressQueryRequest();
        query.setGrade(grade);
        query.setClassName(className);
        query.setPoliticalStatus(politicalStatus);
        query.setKeyword(keyword);
        return Result.success(PageResult.of(studentProgressService.getStudentProgress(page, pageSize, query)));
    }
}
