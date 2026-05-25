package com.ischool.isp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ischool.isp.common.BusinessException;
import com.ischool.isp.dto.request.ProcessCreateRequest;
import com.ischool.isp.dto.request.ProcessNodesUpdateRequest;
import com.ischool.isp.dto.request.ProcessUpdateRequest;
import com.ischool.isp.dto.response.ProcessNodeResponse;
import com.ischool.isp.dto.response.ProcessResponse;
import com.ischool.isp.entity.Process;
import com.ischool.isp.entity.ProcessNode;
import com.ischool.isp.mapper.ProcessMapper;
import com.ischool.isp.mapper.ProcessNodeMapper;
import com.ischool.isp.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final ProcessMapper processMapper;
    private final ProcessNodeMapper processNodeMapper;

    @Override
    public IPage<ProcessResponse> listProcesses(Integer page, Integer pageSize, String keyword, String status) {
        LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<Process>()
                .eq(StringUtils.hasText(status), Process::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Process::getName, keyword)
                        .or()
                        .like(Process::getType, keyword))
                .orderByDesc(Process::getCreatedAt);

        IPage<Process> result = processMapper.selectPage(new Page<>(page, pageSize), wrapper);
        return result.convert(this::toResponse);
    }

    @Override
    public List<ProcessResponse> listEnabledProcesses(String status) {
        String st = StringUtils.hasText(status) ? status : "ENABLED";
        LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<Process>()
                .eq(Process::getStatus, st)
                .orderByDesc(Process::getCreatedAt);
        return processMapper.selectList(wrapper).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ProcessResponse create(ProcessCreateRequest request) {
        Process process = new Process();
        process.setName(request.getName());
        process.setType(request.getType());
        process.setDescription(request.getDescription());
        process.setStatus("ENABLED");
        process.setVersion(1);
        processMapper.insert(process);
        return toResponse(processMapper.selectById(process.getId()));
    }

    @Override
    public ProcessResponse update(Long id, ProcessUpdateRequest request) {
        Process process = processMapper.selectById(id);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }
        process.setName(request.getName());
        process.setDescription(request.getDescription());
        process.setStatus(request.getStatus());
        processMapper.updateById(process);
        return toResponse(processMapper.selectById(id));
    }

    @Override
    public List<ProcessNodeResponse> listNodes(Long processId) {
        List<ProcessNode> nodes = processNodeMapper.selectList(new LambdaQueryWrapper<ProcessNode>()
                .eq(ProcessNode::getProcessId, processId)
                .orderByAsc(ProcessNode::getNodeOrder));
        return nodes.stream().map(n -> ProcessNodeResponse.builder()
                .id(n.getId())
                .processId(n.getProcessId())
                .nodeName(n.getNodeName())
                .nodeOrder(n.getNodeOrder())
                .approverRole(n.getApproverRole())
                .requiredMaterial(n.getRequiredMaterial())
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateNodes(Long processId, ProcessNodesUpdateRequest request) {
        Process process = processMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }

        processNodeMapper.delete(new LambdaQueryWrapper<ProcessNode>().eq(ProcessNode::getProcessId, processId));
        for (ProcessNodesUpdateRequest.ProcessNodeItem item : request.getNodes()) {
            ProcessNode node = new ProcessNode();
            node.setProcessId(processId);
            node.setNodeName(item.getNodeName());
            node.setNodeOrder(item.getNodeOrder());
            node.setApproverRole(item.getApproverRole());
            node.setRequiredMaterial(item.getRequiredMaterial());
            processNodeMapper.insert(node);
        }
    }

    private ProcessResponse toResponse(Process p) {
        if (p == null) return null;
        return ProcessResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .type(p.getType())
                .description(p.getDescription())
                .status(p.getStatus())
                .version(p.getVersion())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

