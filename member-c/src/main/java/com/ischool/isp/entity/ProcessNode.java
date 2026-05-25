package com.ischool.isp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("process_node")
public class ProcessNode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long processId;

    private String nodeName;

    private Integer nodeOrder;

    private String requiredMaterial;

    private String approverRole;
}

