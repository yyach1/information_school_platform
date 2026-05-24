package com.ischool.isp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ischool.isp.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT COUNT(*) FROM notification WHERE receiver_id = #{receiverId} AND read_status = 'UNREAD' AND notification_type = #{type}")
    Long countUnreadByType(@Param("receiverId") Long receiverId, @Param("type") String type);
}
