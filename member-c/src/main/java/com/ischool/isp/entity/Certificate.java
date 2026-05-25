package com.ischool.isp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("certificate_application")
public class Certificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private String certType;
    private String title;
    private String description;
    private String attachmentUrl;
    private String attachmentName;
    private String status;
    private Long approverId;
    private String approveComment;
    private String pdfUrl;
    private LocalDateTime applyTime;
    private LocalDateTime approveTime;
    private LocalDateTime issueTime;
    private LocalDateTime updateTime;
}
