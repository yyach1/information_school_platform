package com.ischool.isp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Select("select count(*) from student")
    Long countStudents();

    @Select("select count(*) from material")
    Long countMaterials();

    @Select("select count(*) from material where status = #{status}")
    Long countMaterialsByStatus(@Param("status") String status);

    @Select("select count(*) from certificate_application")
    Long countCertificates();

    @Select("select count(*) from certificate_application where status = #{status}")
    Long countCertificatesByStatus(@Param("status") String status);

    @Select("select count(*) from file_record where status = 'ACTIVE' and created_at >= #{startTime}")
    Long countUploadsSince(@Param("startTime") LocalDateTime startTime);

    @Select("select coalesce(sum(file_size), 0) from file_record where status = 'ACTIVE'")
    Long sumActiveFileSize();

    @Select("select status as name, count(*) as count from material group by status order by status")
    List<Map<String, Object>> countMaterialGroupByStatus();

    @Select("select coalesce(status, 'UNKNOWN') as name, count(*) as count from student_process group by status order by status")
    List<Map<String, Object>> countProcessGroupByStatus();

    @Select("select coalesce(cert_type, 'UNKNOWN') || ':' || coalesce(status, 'UNKNOWN') as name, count(*) as count " +
            "from certificate_application group by cert_type, status order by cert_type, status")
    List<Map<String, Object>> countCertificateGroupByTypeAndStatus();

    @Select("select cast(created_at as date) as stat_date, count(*) as count, coalesce(sum(file_size), 0) as total_size " +
            "from file_record where status = 'ACTIVE' and created_at >= #{startTime} " +
            "group by cast(created_at as date) order by stat_date")
    List<Map<String, Object>> countUploadsByDay(@Param("startTime") LocalDateTime startTime);
}
