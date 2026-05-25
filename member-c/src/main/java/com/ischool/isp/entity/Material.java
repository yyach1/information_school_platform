package com.ischool.isp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("material")
public class Material {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentProcessId;

    private Long studentId;

    private Long nodeId;

    private String materialType;

    private String fileUrl;

    private String fileName;

    private String description;

    private String status;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

