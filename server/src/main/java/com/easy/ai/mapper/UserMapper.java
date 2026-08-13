package com.easy.ai.mapper;

import com.easy.ai.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    public List<User> findAll();

    @Select("select * from user where email=#{email}")
    public User findUserByEmail(String email);

    @Select("select * from user where open_id=#{openId}")
    public User findUserByOpenId(String openId);

}
