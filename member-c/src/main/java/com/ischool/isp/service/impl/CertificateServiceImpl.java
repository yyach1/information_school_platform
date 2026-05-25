package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.request.*;
import com.ischool.isp.dto.response.*;
import com.ischool.isp.entity.Certificate;
import com.ischool.isp.entity.Student;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.CertificateMapper;
import com.ischool.isp.mapper.StudentMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.security.SecurityUtils;
import com.ischool.isp.service.CertificateService;
import com.ischool.isp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper certificateMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    private static final Map<String, String[]> CERT_TYPE_META = new LinkedHashMap<>();
    static {
        CERT_TYPE_META.put("ENROLLMENT",  new String[]{"在学证明", "用于证明学生当前在校就读状态", "false"});
        CERT_TYPE_META.put("LEAVE",       new String[]{"请假申请", "学生因事/因病请假的审批申请", "true"});
        CERT_TYPE_META.put("SEAL",        new String[]{"盖章申请", "各类表格、材料的学院盖章审批", "true"});
        CERT_TYPE_META.put("PARTY",       new String[]{"党员证明", "证明党员身份及入党时间等信息", "false"});
        CERT_TYPE_META.put("TRANSCRIPT",  new String[]{"成绩单证明", "申请学院出具的成绩排名或成绩证明", "false"});
    }

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "PENDING", "待审核",
            "APPROVED", "已通过",
            "RETURNED", "已退回",
            "ISSUED", "已发放"
    );

    @Override
    public List<CertificateTypeResponse> getCertificateTypes() {
        List<CertificateTypeResponse> list = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : CERT_TYPE_META.entrySet()) {
            String[] meta = entry.getValue();
            list.add(CertificateTypeResponse.builder()
                    .certType(entry.getKey())
                    .label(meta[0])
                    .description(meta[1])
                    .requireAttachment(Boolean.valueOf(meta[2]))
                    .build());
        }
        return list;
    }

    @Override
    @Transactional
    public CertificateResponse apply(CertificateApplyRequest request) {
        String certType = request.getCertType();
        if (!CERT_TYPE_META.containsKey(certType)) {
            throw new BusinessException("CERT_TYPE_INVALID: 无效的证书类型");
        }
        String[] meta = CERT_TYPE_META.get(certType);
        if (Boolean.parseBoolean(meta[2]) && !StringUtils.hasText(request.getAttachmentUrl())) {
            throw new BusinessException("CERT_ATTACHMENT_REQUIRED: 该类型必须上传附件");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        Student student = getCurrentStudent(userId);

        Certificate cert = new Certificate();
        cert.setStudentId(student.getId());
        cert.setCertType(certType);
        cert.setTitle(request.getTitle());
        cert.setDescription(request.getDescription());
        cert.setAttachmentUrl(request.getAttachmentUrl());
        cert.setAttachmentName(request.getAttachmentName());
        cert.setStatus("PENDING");
        cert.setApplyTime(LocalDateTime.now());
        cert.setUpdateTime(LocalDateTime.now());

        certificateMapper.insert(cert);
        notifyTeachersForNewApplication(cert, student);

        return buildDetailResponse(cert, student);
    }

    @Override
    public IPage<CertificateListItemResponse> listMyCertificates(Integer page, Integer pageSize, String status, String certType) {
        Long userId = SecurityUtils.getCurrentUserId();
        Student student = requireStudent(userId);

        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getStudentId, student.getId())
                .eq(StringUtils.hasText(status), Certificate::getStatus, status)
                .eq(StringUtils.hasText(certType), Certificate::getCertType, certType)
                .orderByDesc(Certificate::getApplyTime);

        Page<Certificate> certPage = new Page<>(page, pageSize);
        IPage<Certificate> result = certificateMapper.selectPage(certPage, wrapper);

        return result.convert(c -> CertificateListItemResponse.builder()
                .id(c.getId())
                .certType(c.getCertType())
                .certTypeLabel(getCertLabel(c.getCertType()))
                .title(c.getTitle())
                .status(c.getStatus())
                .statusLabel(STATUS_LABELS.getOrDefault(c.getStatus(), c.getStatus()))
                .applyTime(c.getApplyTime())
                .build());
    }

    @Override
    public CertificateResponse getDetail(Long id) {
        Certificate cert = certificateMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException(404, "CERT_NOT_FOUND: 申请记录不存在");
        }

        Student student = studentMapper.selectById(cert.getStudentId());
        if (student == null) {
            throw new BusinessException(404, "学生信息不存在");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();
        if ("STUDENT".equals(role)) {
            Student currentStudent = requireStudent(userId);
            if (!cert.getStudentId().equals(currentStudent.getId())) {
                throw new BusinessException(403, "无权查看此申请");
            }
        }

        return buildDetailResponse(cert, student);
    }

    @Override
    @Transactional
    public CertificateResponse resubmit(Long id, CertificateResubmitRequest request) {
        Certificate cert = certificateMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException(404, "CERT_NOT_FOUND: 申请记录不存在");
        }
        if (!"RETURNED".equals(cert.getStatus())) {
            throw new BusinessException("CERT_STATUS_CONFLICT: 仅退回状态的申请可重新提交");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        Student student = requireStudent(userId);
        if (!cert.getStudentId().equals(student.getId())) {
            throw new BusinessException(403, "无权操作此申请");
        }

        cert.setTitle(request.getTitle());
        cert.setDescription(request.getDescription());
        cert.setAttachmentUrl(request.getAttachmentUrl());
        cert.setAttachmentName(request.getAttachmentName());
        cert.setStatus("PENDING");
        cert.setApproveComment(null);
        cert.setApproverId(null);
        cert.setUpdateTime(LocalDateTime.now());
        certificateMapper.updateById(cert);

        notifyTeachersForNewApplication(cert, student);

        return buildDetailResponse(cert, student);
    }

    @Override
    public CertificateResponse downloadPdfUrl(Long id) {
        Certificate cert = certificateMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException(404, "CERT_NOT_FOUND: 申请记录不存在");
        }
        if (!"ISSUED".equals(cert.getStatus())) {
            throw new BusinessException("CERT_DOWNLOAD_FORBIDDEN: 非ISSUED状态不可下载");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        Student student = requireStudent(userId);
        if (!cert.getStudentId().equals(student.getId())) {
            throw new BusinessException(403, "无权操作此申请");
        }

        return buildDetailResponse(cert, student);
    }

    @Override
    public IPage<CertificateResponse> adminList(Integer page, Integer pageSize, String status, String certType, String grade, String className) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<Certificate>()
                .eq(StringUtils.hasText(status), Certificate::getStatus, status)
                .eq(StringUtils.hasText(certType), Certificate::getCertType, certType)
                .orderByDesc(Certificate::getApplyTime);

        if (StringUtils.hasText(grade) || StringUtils.hasText(className)) {
            LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<Student>()
                    .eq(StringUtils.hasText(grade), Student::getGrade, grade)
                    .eq(StringUtils.hasText(className), Student::getClassName, className);
            List<Long> studentIds = studentMapper.selectList(studentWrapper).stream()
                    .map(Student::getId)
                    .collect(Collectors.toList());
            if (studentIds.isEmpty()) {
                Page<Certificate> empty = new Page<>(page, pageSize);
                empty.setTotal(0);
                return new Page<CertificateResponse>(page, pageSize).convert(c -> null);
            }
            wrapper.in(Certificate::getStudentId, studentIds);
        }

        Page<Certificate> certPage = new Page<>(page, pageSize);
        IPage<Certificate> result = certificateMapper.selectPage(certPage, wrapper);

        Map<Long, Student> studentMap = new HashMap<>();
        return result.convert(c -> {
            Student student = studentMap.computeIfAbsent(c.getStudentId(), sid -> studentMapper.selectById(sid));
            return buildDetailResponse(c, student);
        });
    }

    @Override
    @Transactional
    public void audit(Long id, CertificateAuditRequest request) {
        Certificate cert = certificateMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException(404, "CERT_NOT_FOUND: 申请记录不存在");
        }
        if (!"PENDING".equals(cert.getStatus())) {
            throw new BusinessException("CERT_STATUS_CONFLICT: 仅待审核状态的申请可审核");
        }

        String result = request.getResult();
        if (!"APPROVED".equals(result) && !"RETURNED".equals(result)) {
            throw new BusinessException("审核结果必须为 APPROVED 或 RETURNED");
        }
        if ("RETURNED".equals(result) && !StringUtils.hasText(request.getComment())) {
            throw new BusinessException("CERT_RETURN_COMMENT_REQUIRED: 退回时必须填写原因");
        }

        cert.setStatus(result);
        cert.setApproverId(SecurityUtils.getCurrentUserId());
        cert.setApproveComment(request.getComment());
        cert.setApproveTime(LocalDateTime.now());
        cert.setUpdateTime(LocalDateTime.now());
        certificateMapper.updateById(cert);

        Student student = studentMapper.selectById(cert.getStudentId());
        notifyStudentAuditResult(cert, student);
    }

    @Override
    @Transactional
    public void issue(Long id, CertificateIssueRequest request) {
        Certificate cert = certificateMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException(404, "CERT_NOT_FOUND: 申请记录不存在");
        }
        if (!"APPROVED".equals(cert.getStatus())) {
            throw new BusinessException("CERT_STATUS_CONFLICT: 仅已通过状态的申请可发放");
        }
        if (!StringUtils.hasText(request.getPdfUrl())) {
            throw new BusinessException("CERT_PDF_REQUIRED: 发放时PDF地址必填");
        }

        cert.setStatus("ISSUED");
        cert.setPdfUrl(request.getPdfUrl());
        cert.setIssueTime(LocalDateTime.now());
        cert.setUpdateTime(LocalDateTime.now());
        certificateMapper.updateById(cert);

        Student student = studentMapper.selectById(cert.getStudentId());
        notifyStudentIssued(cert, student);
    }

    @Override
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (String status : List.of("PENDING", "APPROVED", "RETURNED", "ISSUED")) {
            Long count = certificateMapper.selectCount(
                    new LambdaQueryWrapper<Certificate>().eq(Certificate::getStatus, status));
            stats.put(status, count);
        }
        return stats;
    }

    // ── private helpers ──

    private Student getCurrentStudent(Long userId) {
        return requireStudent(userId);
    }

    private Student requireStudent(Long userId) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
        if (student == null) {
            throw new BusinessException(404, "学生信息不存在");
        }
        return student;
    }

    private CertificateResponse buildDetailResponse(Certificate cert, Student student) {
        User studentUser = userMapper.selectById(student.getUserId());
        String studentName = studentUser != null ? studentUser.getRealName() : "";
        String studentNo = student.getStudentNo() != null ? student.getStudentNo() : "";
        String className = student.getClassName() != null ? student.getClassName() : "";

        String approverName = "";
        if (cert.getApproverId() != null) {
            User approver = userMapper.selectById(cert.getApproverId());
            if (approver != null) {
                approverName = approver.getRealName();
            }
        }

        List<CertificateResponse.ProgressNode> nodes = buildProgressNodes(cert);

        return CertificateResponse.builder()
                .id(cert.getId())
                .certType(cert.getCertType())
                .certTypeLabel(getCertLabel(cert.getCertType()))
                .title(cert.getTitle())
                .description(cert.getDescription())
                .attachmentUrl(cert.getAttachmentUrl())
                .attachmentName(cert.getAttachmentName())
                .status(cert.getStatus())
                .statusLabel(STATUS_LABELS.getOrDefault(cert.getStatus(), cert.getStatus()))
                .studentName(studentName)
                .studentNo(studentNo)
                .className(className)
                .approverName(approverName)
                .approveComment(cert.getApproveComment())
                .pdfUrl(cert.getPdfUrl())
                .applyTime(cert.getApplyTime())
                .approveTime(cert.getApproveTime())
                .issueTime(cert.getIssueTime())
                .progressNodes(nodes)
                .build();
    }

    private List<CertificateResponse.ProgressNode> buildProgressNodes(Certificate cert) {
        List<CertificateResponse.ProgressNode> nodes = new ArrayList<>();

        nodes.add(CertificateResponse.ProgressNode.builder()
                .type("APPLIED")
                .time(cert.getApplyTime())
                .message("提交申请")
                .label("已提交")
                .build());

        if ("RETURNED".equals(cert.getStatus())) {
            nodes.add(CertificateResponse.ProgressNode.builder()
                    .type("RETURNED")
                    .time(cert.getApproveTime())
                    .message("退回: " + (cert.getApproveComment() != null ? cert.getApproveComment() : ""))
                    .label("已退回")
                    .build());
        } else if ("APPROVED".equals(cert.getStatus()) || "ISSUED".equals(cert.getStatus())) {
            nodes.add(CertificateResponse.ProgressNode.builder()
                    .type("APPROVED")
                    .time(cert.getApproveTime())
                    .message("审核通过")
                    .label("已通过")
                    .build());
        }

        if ("ISSUED".equals(cert.getStatus())) {
            nodes.add(CertificateResponse.ProgressNode.builder()
                    .type("ISSUED")
                    .time(cert.getIssueTime())
                    .message("已发放")
                    .label("已发放")
                    .build());
        }

        return nodes;
    }

    private void notifyTeachersForNewApplication(Certificate cert, Student student) {
        User studentUser = userMapper.selectById(student.getUserId());
        String studentName = studentUser != null ? studentUser.getRealName() : "学生";
        String certLabel = getCertLabel(cert.getCertType());

        List<User> receivers = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, "TEACHER"));
        if (receivers.isEmpty()) {
            receivers = userMapper.selectList(
                    new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
        }
        for (User u : receivers) {
            NotificationCreateRequest req = new NotificationCreateRequest();
            req.setReceiverId(u.getId());
            req.setNotificationType("TODO");
            req.setTitle("新的证明申请待审核");
            req.setContent(studentName + "提交了" + certLabel + "申请，请及时处理");
            req.setBizType("CERT_APPLICATION_SUBMITTED");
            req.setBizId(cert.getId());
            notificationService.createNotification(req);
        }
    }

    private void notifyStudentAuditResult(Certificate cert, Student student) {
        String certLabel = getCertLabel(cert.getCertType());
        String title = "证明申请审核结果";
        String content;
        if ("APPROVED".equals(cert.getStatus())) {
            content = "你的" + certLabel + "申请已通过审核";
        } else {
            content = "你的" + certLabel + "申请被退回";
            if (StringUtils.hasText(cert.getApproveComment())) {
                content += "，原因：" + cert.getApproveComment();
            }
        }

        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setReceiverId(student.getUserId());
        req.setNotificationType("NOTICE");
        req.setTitle(title);
        req.setContent(content);
        req.setBizType("CERT_APPLICATION_AUDITED");
        req.setBizId(cert.getId());
        notificationService.createNotification(req);
    }

    private void notifyStudentIssued(Certificate cert, Student student) {
        String certLabel = getCertLabel(cert.getCertType());
        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setReceiverId(student.getUserId());
        req.setNotificationType("NOTICE");
        req.setTitle("电子证明已发放");
        req.setContent("你的" + certLabel + "证明已发放，可在线下载");
        req.setBizType("CERT_APPLICATION_ISSUED");
        req.setBizId(cert.getId());
        notificationService.createNotification(req);
    }

    private String getCertLabel(String certType) {
        String[] meta = CERT_TYPE_META.get(certType);
        return meta != null ? meta[0] : certType;
    }
}
