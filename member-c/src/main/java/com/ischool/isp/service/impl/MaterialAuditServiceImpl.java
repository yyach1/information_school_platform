package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.request.MaterialAuditRequest;
import com.ischool.isp.dto.request.NotificationCreateRequest;
import com.ischool.isp.dto.response.AdminMaterialDetailResponse;
import com.ischool.isp.dto.response.AdminMaterialListItemResponse;
import com.ischool.isp.entity.ApprovalRecord;
import com.ischool.isp.entity.Material;
import com.ischool.isp.entity.Process;
import com.ischool.isp.entity.ProcessNode;
import com.ischool.isp.entity.Student;
import com.ischool.isp.entity.StudentProcess;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.ApprovalRecordMapper;
import com.ischool.isp.mapper.MaterialMapper;
import com.ischool.isp.mapper.ProcessMapper;
import com.ischool.isp.mapper.ProcessNodeMapper;
import com.ischool.isp.mapper.StudentMapper;
import com.ischool.isp.mapper.StudentProcessMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.security.SecurityUtils;
import com.ischool.isp.service.MaterialAuditService;
import com.ischool.isp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialAuditServiceImpl implements MaterialAuditService {

    private final MaterialMapper materialMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final StudentProcessMapper studentProcessMapper;
    private final ProcessMapper processMapper;
    private final ProcessNodeMapper processNodeMapper;
    private final NotificationService notificationService;

    @Override
    public List<AdminMaterialListItemResponse> listMaterials(Integer page, Integer pageSize, String status, String grade, String className, String processType) {
        long offset = (long) (page - 1) * pageSize;
        return materialMapper.listAdminMaterials(status, grade, className, processType, pageSize, offset);
    }

    @Override
    public long countMaterials(String status, String grade, String className, String processType) {
        return materialMapper.countAdminMaterials(status, grade, className, processType);
    }

    @Override
    public AdminMaterialDetailResponse getMaterialDetail(Long materialId) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new BusinessException(404, "材料不存在");
        }

        Student student = studentMapper.selectById(material.getStudentId());
        User studentUser = student != null ? userMapper.selectById(student.getUserId()) : null;
        StudentProcess sp = material.getStudentProcessId() != null ? studentProcessMapper.selectById(material.getStudentProcessId()) : null;
        Process process = sp != null ? processMapper.selectById(sp.getProcessId()) : null;
        ProcessNode node = material.getNodeId() != null ? processNodeMapper.selectById(material.getNodeId()) : null;

        List<ApprovalRecord> approvals = approvalRecordMapper.selectList(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getMaterialId, materialId)
                .orderByDesc(ApprovalRecord::getApproveTime));
        List<Long> approverIds = approvals.stream().map(ApprovalRecord::getApproverId).distinct().toList();
        Map<Long, User> approverMap = approverIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(approverIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<AdminMaterialDetailResponse.ApprovalRecordResponse> approvalResponses = approvals.stream()
                .map(a -> {
                    User approver = approverMap.get(a.getApproverId());
                    return AdminMaterialDetailResponse.ApprovalRecordResponse.builder()
                            .id(a.getId())
                            .approverId(a.getApproverId())
                            .approverName(approver != null ? approver.getRealName() : null)
                            .result(a.getResult())
                            .comment(a.getComment())
                            .approveTime(a.getApproveTime())
                            .build();
                }).toList();

        return AdminMaterialDetailResponse.builder()
                .id(material.getId())
                .studentProcessId(material.getStudentProcessId())
                .studentId(material.getStudentId())
                .studentNo(student != null ? student.getStudentNo() : null)
                .studentName(studentUser != null ? studentUser.getRealName() : null)
                .grade(student != null ? student.getGrade() : null)
                .className(student != null ? student.getClassName() : null)
                .processType(process != null ? process.getType() : null)
                .processName(process != null ? process.getName() : null)
                .nodeName(node != null ? node.getNodeName() : null)
                .materialType(material.getMaterialType())
                .fileUrl(material.getFileUrl())
                .fileName(material.getFileName())
                .description(material.getDescription())
                .status(material.getStatus())
                .submitTime(material.getSubmitTime())
                .approvals(approvalResponses)
                .build();
    }

    @Override
    @Transactional
    public void auditMaterial(Long materialId, MaterialAuditRequest request) {
        Material material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new BusinessException(404, "材料不存在");
        }
        if (!"PENDING".equals(material.getStatus())) {
            throw new BusinessException("仅支持审核待审核状态材料");
        }

        String result = request.getResult();
        if (!"APPROVED".equals(result) && !"RETURNED".equals(result)) {
            throw new BusinessException("result取值不合法");
        }
        if ("RETURNED".equals(result) && (request.getComment() == null || request.getComment().trim().isEmpty())) {
            throw new BusinessException("退回原因不能为空");
        }

        Long approverId = SecurityUtils.getCurrentUserId();
        if (approverId == null) {
            throw new BusinessException(401, "未登录");
        }

        LocalDateTime now = LocalDateTime.now();
        ApprovalRecord record = new ApprovalRecord();
        record.setMaterialId(material.getId());
        record.setApproverId(approverId);
        record.setResult(result);
        record.setComment(request.getComment());
        record.setApproveTime(now);
        approvalRecordMapper.insert(record);

        material.setStatus(result);
        material.setUpdatedAt(now);
        materialMapper.updateById(material);

        StudentProcess sp = material.getStudentProcessId() != null ? studentProcessMapper.selectById(material.getStudentProcessId()) : null;
        if (sp != null) {
            sp.setUpdateTime(now);
            studentProcessMapper.updateById(sp);
            if ("APPROVED".equals(result)) {
                tryAdvanceStudentProcess(sp);
            }
        }

        notifyStudent(material, result, request.getComment());
    }

    private void notifyStudent(Material material, String result, String comment) {
        Student student = studentMapper.selectById(material.getStudentId());
        if (student == null) return;
        User user = userMapper.selectById(student.getUserId());
        if (user == null) return;

        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setReceiverId(user.getId());
        req.setNotificationType("NOTICE");
        req.setBizType("MATERIAL");
        req.setBizId(material.getId());
        if ("APPROVED".equals(result)) {
            req.setTitle("材料审核通过");
            req.setContent("你的材料已审核通过");
        } else {
            req.setTitle("材料被退回");
            req.setContent("退回原因：" + comment);
        }
        notificationService.createNotification(req);
    }

    private void tryAdvanceStudentProcess(StudentProcess sp) {
        if (!"IN_PROGRESS".equals(sp.getStatus())) return;
        if (sp.getCurrentNodeId() == null) return;
        ProcessNode current = processNodeMapper.selectById(sp.getCurrentNodeId());
        if (current == null) return;

        List<ProcessNode> nodes = processNodeMapper.selectList(new LambdaQueryWrapper<ProcessNode>()
                .eq(ProcessNode::getProcessId, sp.getProcessId())
                .orderByAsc(ProcessNode::getNodeOrder));
        Map<Integer, ProcessNode> orderMap = nodes.stream()
                .filter(n -> n.getNodeOrder() != null)
                .collect(Collectors.toMap(ProcessNode::getNodeOrder, n -> n, (a, b) -> a));

        boolean allApproved = materialMapper.selectList(new LambdaQueryWrapper<Material>()
                        .eq(Material::getStudentProcessId, sp.getId())
                        .eq(Material::getNodeId, sp.getCurrentNodeId()))
                .stream()
                .allMatch(m -> "APPROVED".equals(m.getStatus()));
        if (!allApproved) return;

        Integer nextOrder = current.getNodeOrder() == null ? null : current.getNodeOrder() + 1;
        ProcessNode next = nextOrder != null ? orderMap.get(nextOrder) : null;
        if (next == null) {
            sp.setStatus("COMPLETED");
            sp.setCurrentNodeId(null);
            studentProcessMapper.updateById(sp);
            return;
        }
        sp.setCurrentNodeId(next.getId());
        studentProcessMapper.updateById(sp);

        List<User> approvers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "TEACHER"));
        if (approvers.isEmpty()) {
            approvers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
        }
        Process process = processMapper.selectById(sp.getProcessId());
        String processName = process != null && process.getName() != null ? process.getName() : "流程";
        Student student = studentMapper.selectById(sp.getStudentId());
        User studentUser = student != null ? userMapper.selectById(student.getUserId()) : null;
        String studentName = studentUser != null && studentUser.getRealName() != null ? studentUser.getRealName() : "学生";
        for (User approver : approvers) {
            NotificationCreateRequest req = new NotificationCreateRequest();
            req.setReceiverId(approver.getId());
            req.setNotificationType("TODO");
            req.setTitle("待办审核");
            req.setContent(studentName + "的" + processName + "已进入下一节点：" + next.getNodeName());
            req.setBizType("STUDENT_PROCESS");
            req.setBizId(sp.getId());
            notificationService.createNotification(req);
        }
    }
}

