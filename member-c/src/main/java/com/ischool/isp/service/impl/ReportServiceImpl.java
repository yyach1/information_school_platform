package com.ischool.isp.service.impl;

import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.response.ReportCountItem;
import com.ischool.isp.dto.response.ReportOverviewResponse;
import com.ischool.isp.dto.response.UploadTrendItem;
import com.ischool.isp.mapper.ReportMapper;
import com.ischool.isp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    public ReportOverviewResponse overview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return ReportOverviewResponse.builder()
                .studentCount(nvl(reportMapper.countStudents()))
                .materialCount(nvl(reportMapper.countMaterials()))
                .pendingMaterialCount(nvl(reportMapper.countMaterialsByStatus("PENDING")))
                .approvedMaterialCount(nvl(reportMapper.countMaterialsByStatus("APPROVED")))
                .returnedMaterialCount(nvl(reportMapper.countMaterialsByStatus("RETURNED")))
                .certificateCount(nvl(reportMapper.countCertificates()))
                .pendingCertificateCount(nvl(reportMapper.countCertificatesByStatus("PENDING")))
                .todayUploadCount(nvl(reportMapper.countUploadsSince(todayStart)))
                .totalFileSize(nvl(reportMapper.sumActiveFileSize()))
                .build();
    }

    @Override
    public List<ReportCountItem> materialStatus() {
        return toCountItems(reportMapper.countMaterialGroupByStatus());
    }

    @Override
    public List<ReportCountItem> processStatus() {
        return toCountItems(reportMapper.countProcessGroupByStatus());
    }

    @Override
    public List<ReportCountItem> certificateStatus() {
        return toCountItems(reportMapper.countCertificateGroupByTypeAndStatus());
    }

    @Override
    public List<UploadTrendItem> uploadTrend(Integer days) {
        int safeDays = days == null ? 7 : Math.max(1, Math.min(days, 90));
        LocalDateTime startTime = LocalDate.now().minusDays(safeDays - 1L).atStartOfDay();
        List<Map<String, Object>> rows = reportMapper.countUploadsByDay(startTime);
        List<UploadTrendItem> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(UploadTrendItem.builder()
                    .date(String.valueOf(get(row, "stat_date")))
                    .count(asLong(get(row, "count")))
                    .totalSize(asLong(get(row, "total_size")))
                    .build());
        }
        return result;
    }

    @Override
    public byte[] exportCsv(String type) {
        String safeType = type == null ? "overview" : type.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder("\uFEFF");
        switch (safeType) {
            case "overview" -> {
                ReportOverviewResponse o = overview();
                sb.append("指标,数值\n");
                append(sb, "学生总数", o.getStudentCount());
                append(sb, "材料总数", o.getMaterialCount());
                append(sb, "待审核材料数", o.getPendingMaterialCount());
                append(sb, "已通过材料数", o.getApprovedMaterialCount());
                append(sb, "已退回材料数", o.getReturnedMaterialCount());
                append(sb, "证明申请数", o.getCertificateCount());
                append(sb, "待处理证明数", o.getPendingCertificateCount());
                append(sb, "今日上传数", o.getTodayUploadCount());
                append(sb, "文件总字节数", o.getTotalFileSize());
            }
            case "material-status" -> appendCountItems(sb, "材料状态", materialStatus());
            case "process-status" -> appendCountItems(sb, "流程状态", processStatus());
            case "certificate-status" -> appendCountItems(sb, "证明类型与状态", certificateStatus());
            case "upload-trend" -> {
                sb.append("日期,上传数,总字节数\n");
                for (UploadTrendItem item : uploadTrend(30)) {
                    sb.append(csv(item.getDate())).append(',')
                            .append(item.getCount()).append(',')
                            .append(item.getTotalSize()).append('\n');
                }
            }
            default -> throw new BusinessException("不支持的导出类型：" + type);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendCountItems(StringBuilder sb, String title, List<ReportCountItem> items) {
        sb.append(title).append(",数量\n");
        for (ReportCountItem item : items) {
            sb.append(csv(item.getName())).append(',').append(item.getCount()).append('\n');
        }
    }

    private void append(StringBuilder sb, String key, Long value) {
        sb.append(csv(key)).append(',').append(value).append('\n');
    }

    private List<ReportCountItem> toCountItems(List<Map<String, Object>> rows) {
        List<ReportCountItem> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object name = get(row, "name");
            Object count = get(row, "count");
            result.add(ReportCountItem.builder()
                    .name(String.valueOf(name))
                    .count(asLong(count))
                    .build());
        }
        return result;
    }

    private Object get(Map<String, Object> row, String key) {
        if (row.containsKey(key)) {
            return row.get(key);
        }
        String upper = key.toUpperCase(Locale.ROOT);
        if (row.containsKey(upper)) {
            return row.get(upper);
        }
        String camel = toCamel(key);
        if (row.containsKey(camel)) {
            return row.get(camel);
        }
        return null;
    }

    private String toCamel(String value) {
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : value.toCharArray()) {
            if (c == '_') {
                upperNext = true;
            } else if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Long asLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Long nvl(Long value) {
        return value == null ? 0L : value;
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
