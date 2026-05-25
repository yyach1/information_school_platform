package com.ischool.isp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_process")
public class StudentProcess {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long processId;

    private Long currentNodeId;

    private String status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}

