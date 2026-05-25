package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.request.MaterialSubmitRequest;
import com.ischool.isp.dto.request.NotificationCreateRequest;
import com.ischool.isp.dto.response.NodeRequirementsResponse;
import com.ischool.isp.dto.response.StudentProcessDetailResponse;
import com.ischool.isp.dto.response.TimelineResponse;
import com.ischool.isp.entity.Material;
import com.ischool.isp.entity.Process;
import com.ischool.isp.entity.ProcessNode;
import com.ischool.isp.entity.Student;
import com.ischool.isp.entity.StudentProcess;
import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.MaterialMapper;
import com.ischool.isp.mapper.ProcessMapper;
import com.ischool.isp.mapper.ProcessNodeMapper;
import com.ischool.isp.mapper.StudentMapper;
import com.ischool.isp.mapper.StudentProcessMapper;
import com.ischool.isp.mapper.UserMapper;
import com.ischool.isp.security.SecurityUtils;
import com.ischool.isp.service.NotificationService;
import com.ischool.isp.service.StudentProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProcessServiceImpl implements StudentProcessService {

    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final ProcessMapper processMapper;
    private final ProcessNodeMapper processNodeMapper;
    private final StudentProcessMapper studentProcessMapper;
    private final MaterialMapper materialMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long getCurrentStudentId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
        if (student == null) {
            throw new BusinessException(403, "当前用户不是学生");
        }
        return student.getId();
    }

    @Override
    @Transactional
    public Long createOrGetStudentProcess(Long processId) {
        Long studentId = getCurrentStudentId();
        Process process = processMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }
        if (!"ENABLED".equals(process.getStatus())) {
            throw new BusinessException("流程未启用");
        }

        StudentProcess existing = studentProcessMapper.selectOne(new LambdaQueryWrapper<StudentProcess>()
                .eq(StudentProcess::getStudentId, studentId)
                .eq(StudentProcess::getProcessId, processId)
                .orderByDesc(StudentProcess::getId)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing.getId();
        }

        List<ProcessNode> nodes = processNodeMapper.selectList(new LambdaQueryWrapper<ProcessNode>()
                .eq(ProcessNode::getProcessId, processId)
                .orderByAsc(ProcessNode::getNodeOrder));
        if (nodes.isEmpty()) {
            throw new BusinessException("流程未配置节点");
        }

        StudentProcess sp = new StudentProcess();
        sp.setStudentId(studentId);
        sp.setProcessId(processId);
        sp.setCurrentNodeId(nodes.get(0).getId());
        sp.setStatus("IN_PROGRESS");
        sp.setStartTime(LocalDateTime.now());
        sp.setUpdateTime(LocalDateTime.now());
        studentProcessMapper.insert(sp);
        return sp.getId();
    }

    @Override
    public StudentProcessDetailResponse getDetail(Long studentProcessId) {
        Long studentId = getCurrentStudentId();
        StudentProcess sp = studentProcessMapper.selectById(studentProcessId);
        if (sp == null || !sp.getStudentId().equals(studentId)) {
            throw new BusinessException(404, "流程记录不存在");
        }

        Process process = processMapper.selectById(sp.getProcessId());
        List<ProcessNode> nodes = processNodeMapper.selectList(new LambdaQueryWrapper<ProcessNode>()
                .eq(ProcessNode::getProcessId, sp.getProcessId())
                .orderByAsc(ProcessNode::getNodeOrder));

        Map<Long, ProcessNode> nodeMap = nodes.stream().collect(Collectors.toMap(ProcessNode::getId, n -> n));
        ProcessNode currentNode = sp.getCurrentNodeId() != null ? nodeMap.get(sp.getCurrentNodeId()) : null;

        Integer currentOrder = currentNode != null ? currentNode.getNodeOrder() : null;
        List<StudentProcessDetailResponse.NodeStatus> nodeStatuses = nodes.stream()
                .map(n -> StudentProcessDetailResponse.NodeStatus.builder()
                        .id(n.getId())
                        .nodeName(n.getNodeName())
                        .nodeOrder(n.getNodeOrder())
                        .status(resolveNodeStatus(n.getNodeOrder(), currentOrder, sp.getStatus()))
                        .build())
                .collect(Collectors.toList());

        List<Material> materials = materialMapper.selectList(new LambdaQueryWrapper<Material>()
                .eq(Material::getStudentProcessId, sp.getId())
                .orderByDesc(Material::getSubmitTime));
        List<StudentProcessDetailResponse.MaterialItem> materialItems = materials.stream()
                .map(m -> StudentProcessDetailResponse.MaterialItem.builder()
                        .id(m.getId())
                        .nodeId(m.getNodeId())
                        .materialType(m.getMaterialType())
                        .fileUrl(m.getFileUrl())
                        .fileName(m.getFileName())
                        .status(m.getStatus())
                        .submitTime(m.getSubmitTime())
                        .build())
                .collect(Collectors.toList());

        return StudentProcessDetailResponse.builder()
                .id(sp.getId())
                .process(StudentProcessDetailResponse.ProcessSummary.builder()
                        .id(process != null ? process.getId() : null)
                        .name(process != null ? process.getName() : null)
                        .type(process != null ? process.getType() : null)
                        .version(process != null ? process.getVersion() : null)
                        .build())
                .status(sp.getStatus())
                .currentNode(currentNode == null ? null : StudentProcessDetailResponse.NodeSummary.builder()
                        .id(currentNode.getId())
                        .nodeName(currentNode.getNodeName())
                        .nodeOrder(currentNode.getNodeOrder())
                        .approverRole(currentNode.getApproverRole())
                        .build())
                .nodes(nodeStatuses)
                .materials(materialItems)
                .startTime(sp.getStartTime())
                .updateTime(sp.getUpdateTime())
                .build();
    }

    @Override
    public NodeRequirementsResponse getCurrentNodeRequirements(Long studentProcessId) {
        Long studentId = getCurrentStudentId();
        StudentProcess sp = studentProcessMapper.selectById(studentProcessId);
        if (sp == null || !sp.getStudentId().equals(studentId)) {
            throw new BusinessException(404, "流程记录不存在");
        }
        if (sp.getCurrentNodeId() == null) {
            return NodeRequirementsResponse.builder()
                    .nodeId(null)
                    .nodeName(null)
                    .requiredMaterial(List.of())
                    .build();
        }
        ProcessNode node = processNodeMapper.selectById(sp.getCurrentNodeId());
        List<NodeRequirementsResponse.RequiredMaterial> materials = parseRequiredMaterial(node != null ? node.getRequiredMaterial() : null);
        return NodeRequirementsResponse.builder()
                .nodeId(node != null ? node.getId() : null)
                .nodeName(node != null ? node.getNodeName() : null)
                .requiredMaterial(materials)
                .build();
    }

    @Override
    @Transactional
    public Long submitMaterial(Long studentProcessId, MaterialSubmitRequest request) {
        Long studentId = getCurrentStudentId();
        StudentProcess sp = studentProcessMapper.selectById(studentProcessId);
        if (sp == null || !sp.getStudentId().equals(studentId)) {
            throw new BusinessException(404, "流程记录不存在");
        }
        if (!"IN_PROGRESS".equals(sp.getStatus())) {
            throw new BusinessException("流程已结束");
        }
        if (sp.getCurrentNodeId() == null || !sp.getCurrentNodeId().equals(request.getNodeId())) {
            throw new BusinessException("当前节点不匹配");
        }

        Material existing = materialMapper.selectOne(new LambdaQueryWrapper<Material>()
                .eq(Material::getStudentProcessId, sp.getId())
                .eq(Material::getNodeId, request.getNodeId())
                .eq(Material::getMaterialType, request.getMaterialType())
                .last("LIMIT 1"));

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            Material material = new Material();
            material.setStudentProcessId(sp.getId());
            material.setStudentId(studentId);
            material.setNodeId(request.getNodeId());
            material.setMaterialType(request.getMaterialType());
            material.setFileUrl(request.getFileUrl());
            material.setFileName(request.getFileName());
            material.setDescription(request.getDescription());
            material.setStatus("PENDING");
            material.setSubmitTime(now);
            material.setUpdatedAt(now);
            materialMapper.insert(material);
            existing = material;
        } else {
            existing.setFileUrl(request.getFileUrl());
            existing.setFileName(request.getFileName());
            existing.setDescription(request.getDescription());
            existing.setStatus("PENDING");
            existing.setSubmitTime(now);
            existing.setUpdatedAt(now);
            materialMapper.updateById(existing);
        }

        sp.setUpdateTime(now);
        studentProcessMapper.updateById(sp);

        createTodoForNextApprover(sp);
        return existing.getId();
    }

    @Override
    public TimelineResponse getTimeline(Long studentProcessId) {
        Long studentId = getCurrentStudentId();
        StudentProcess sp = studentProcessMapper.selectById(studentProcessId);
        if (sp == null || !sp.getStudentId().equals(studentId)) {
            throw new BusinessException(404, "流程记录不存在");
        }

        List<TimelineResponse.TimelineEvent> events = new ArrayList<>();
        List<Material> materials = materialMapper.selectList(new LambdaQueryWrapper<Material>()
                .eq(Material::getStudentProcessId, sp.getId()));
        for (Material m : materials) {
            if (m.getSubmitTime() != null) {
                events.add(TimelineResponse.TimelineEvent.builder()
                        .type("MATERIAL_SUBMITTED")
                        .time(m.getSubmitTime())
                        .message("材料已提交，等待审核")
                        .build());
            }
            if ("RETURNED".equals(m.getStatus()) && m.getUpdatedAt() != null) {
                events.add(TimelineResponse.TimelineEvent.builder()
                        .type("MATERIAL_RETURNED")
                        .time(m.getUpdatedAt())
                        .message("材料被退回，请按要求重新提交")
                        .build());
            }
            if ("APPROVED".equals(m.getStatus()) && m.getUpdatedAt() != null) {
                events.add(TimelineResponse.TimelineEvent.builder()
                        .type("MATERIAL_APPROVED")
                        .time(m.getUpdatedAt())
                        .message("材料已审核通过")
                        .build());
            }
        }

        events.sort(Comparator.comparing(TimelineResponse.TimelineEvent::getTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return TimelineResponse.builder()
                .studentProcessId(sp.getId())
                .events(events)
                .build();
    }

    private String resolveNodeStatus(Integer nodeOrder, Integer currentOrder, String processStatus) {
        if ("COMPLETED".equals(processStatus)) {
            return "DONE";
        }
        if (currentOrder == null) {
            return "PENDING";
        }
        if (nodeOrder < currentOrder) return "DONE";
        if (nodeOrder.equals(currentOrder)) return "CURRENT";
        return "PENDING";
    }

    private List<NodeRequirementsResponse.RequiredMaterial> parseRequiredMaterial(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(raw, new TypeReference<>() {
            });
            return list.stream().map(item -> NodeRequirementsResponse.RequiredMaterial.builder()
                    .materialType(item.get("materialType") != null ? item.get("materialType").toString() : null)
                    .required(item.get("required") == null ? Boolean.TRUE : Boolean.valueOf(item.get("required").toString()))
                    .maxSizeMB(item.get("maxSizeMB") == null ? null : Integer.valueOf(item.get("maxSizeMB").toString()))
                    .ext(item.get("ext") instanceof List<?> extList ? extList.stream().map(Object::toString).collect(Collectors.toList()) : null)
                    .build()).collect(Collectors.toList());
        } catch (Exception ignored) {
            String[] parts = raw.split(",");
            List<NodeRequirementsResponse.RequiredMaterial> result = new ArrayList<>();
            for (String p : parts) {
                String t = p.trim();
                if (!t.isEmpty()) {
                    result.add(NodeRequirementsResponse.RequiredMaterial.builder()
                            .materialType(t)
                            .required(true)
                            .build());
                }
            }
            return result;
        }
    }

    private void createTodoForNextApprover(StudentProcess sp) {
        List<ProcessNode> nodes = processNodeMapper.selectList(new LambdaQueryWrapper<ProcessNode>()
                .eq(ProcessNode::getProcessId, sp.getProcessId())
                .orderByAsc(ProcessNode::getNodeOrder));
        if (nodes.isEmpty()) return;
        ProcessNode current = sp.getCurrentNodeId() == null ? null : nodes.stream()
                .filter(n -> n.getId().equals(sp.getCurrentNodeId()))
                .findFirst().orElse(null);
        if (current == null) return;
        ProcessNode next = nodes.stream()
                .filter(n -> n.getNodeOrder() != null && current.getNodeOrder() != null && n.getNodeOrder() == current.getNodeOrder() + 1)
                .findFirst()
                .orElse(null);
        if (next == null) return;

        Long userId = SecurityUtils.getCurrentUserId();
        Student student = studentMapper.selectById(sp.getStudentId());
        User user = student != null ? userMapper.selectById(student.getUserId()) : null;
        String studentName = user != null && user.getRealName() != null ? user.getRealName() : "学生";
        Process process = processMapper.selectById(sp.getProcessId());
        String processName = process != null && process.getName() != null ? process.getName() : "流程";

        List<User> approvers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "TEACHER"));
        if (approvers.isEmpty()) {
            approvers = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
        }
        for (User approver : approvers) {
            NotificationCreateRequest req = new NotificationCreateRequest();
            req.setReceiverId(approver.getId());
            req.setNotificationType("TODO");
            req.setTitle("材料待审核");
            req.setContent(studentName + "提交了" + processName + "材料，请及时审核");
            req.setBizType("MATERIAL");
            req.setBizId(sp.getId());
            notificationService.createNotification(req);
        }
    }
}

