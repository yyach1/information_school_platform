package com.ischool.isp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ischool.isp.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_record")
public class FileRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文件所有者，对应 user.id。学生上传时默认是当前登录用户。 */
    private Long ownerId;

    /** 关联业务类型，例如 MATERIAL / CERTIFICATE / AVATAR / OTHER。 */
    private String relatedType;

    /** 关联业务 id，例如 material.id 或 certificate_application.id。 */
    private Long relatedId;

    private String originalName;

    private String storedName;

    private String fileUrl;

    private String contentType;

    private Long fileSize;

    /** ACTIVE / DELETED，删除采用逻辑删除，避免历史材料引用失效。 */
    private String status;
}
