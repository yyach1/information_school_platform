package com.ischool.isp.dto.request;

import lombok.Data;

@Data
public class StudentProgressQueryRequest {

    private String grade;
    private String className;
    private String politicalStatus;
    private String keyword;
}
