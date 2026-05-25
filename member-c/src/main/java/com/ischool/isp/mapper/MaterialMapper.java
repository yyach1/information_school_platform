package com.ischool.isp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ischool.isp.dto.response.AdminMaterialListItemResponse;
import com.ischool.isp.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

    @Select("""
            SELECT
              m.id AS id,
              m.student_process_id AS studentProcessId,
              m.student_id AS studentId,
              s.student_no AS studentNo,
              u.real_name AS studentName,
              s.grade AS grade,
              s.class_name AS className,
              p.type AS processType,
              p.name AS processName,
              pn.node_name AS nodeName,
              m.material_type AS materialType,
              m.status AS status,
              m.submit_time AS submitTime
            FROM material m
            JOIN student s ON s.id = m.student_id
            JOIN "user" u ON u.id = s.user_id
            LEFT JOIN student_process sp ON sp.id = m.student_process_id
            LEFT JOIN process p ON p.id = sp.process_id
            LEFT JOIN process_node pn ON pn.id = m.node_id
            WHERE (#{status} IS NULL OR m.status = #{status})
              AND (#{grade} IS NULL OR s.grade = #{grade})
              AND (#{className} IS NULL OR s.class_name = #{className})
              AND (#{processType} IS NULL OR p.type = #{processType})
            ORDER BY m.submit_time DESC
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<AdminMaterialListItemResponse> listAdminMaterials(
            @Param("status") String status,
            @Param("grade") String grade,
            @Param("className") String className,
            @Param("processType") String processType,
            @Param("limit") long limit,
            @Param("offset") long offset
    );

    @Select("""
            SELECT COUNT(1)
            FROM material m
            JOIN student s ON s.id = m.student_id
            LEFT JOIN student_process sp ON sp.id = m.student_process_id
            LEFT JOIN process p ON p.id = sp.process_id
            WHERE (#{status} IS NULL OR m.status = #{status})
              AND (#{grade} IS NULL OR s.grade = #{grade})
              AND (#{className} IS NULL OR s.class_name = #{className})
              AND (#{processType} IS NULL OR p.type = #{processType})
            """)
    long countAdminMaterials(
            @Param("status") String status,
            @Param("grade") String grade,
            @Param("className") String className,
            @Param("processType") String processType
    );
}

