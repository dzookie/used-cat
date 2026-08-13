package com.easy.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.easy.ai.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
