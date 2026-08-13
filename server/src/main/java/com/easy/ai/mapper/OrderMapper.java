package com.easy.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.ai.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE create_time >= #{todayStart} AND status = 1")
    Double selectTodaySales(@Param("todayStart") LocalDateTime todayStart);
}
