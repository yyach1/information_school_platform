package com.ischool.isp.dto.request;

import lombok.Data;

@Data
public class FileQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 10;

    private Long ownerId;

    private String relatedType;

    private Long relatedId;

    private String keyword;
}
