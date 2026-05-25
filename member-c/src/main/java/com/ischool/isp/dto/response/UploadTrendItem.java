package com.ischool.isp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadTrendItem {

    private String date;

    private Long count;

    private Long totalSize;
}
