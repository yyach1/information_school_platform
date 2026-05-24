package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnreadCountResponse {

    private Long noticeCount;
    private Long todoCount;
    private Long totalCount;
}
