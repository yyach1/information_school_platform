package com.ischool.isp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TimelineResponse {

    private Long studentProcessId;
    private List<TimelineEvent> events;

    @Data
    @Builder
    public static class TimelineEvent {
        private String type;
        private LocalDateTime time;
        private String message;
    }
}

