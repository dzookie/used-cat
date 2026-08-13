package com.easy.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.ai.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
