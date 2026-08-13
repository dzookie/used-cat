package com.easy.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.ai.entity.Commodity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommodityMapper extends BaseMapper<Commodity> {

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count FROM commodity WHERE create_time >= #{start} AND create_time <= #{end} GROUP BY DATE(create_time) ORDER BY DATE(create_time)")
    List<Map<String, Object>> selectDailyCountByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
