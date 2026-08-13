package com.easy.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.ai.entity.User;
import com.easy.ai.mapper.UserMapper;
import com.easy.ai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public User findUserByEmail(String email) {
        User user = userMapper.findUserByEmail(email);
        return user;
    }

    @Override
    public User findUserByOpenId(String openId) {
        return userMapper.findUserByOpenId(openId);
    }
}
