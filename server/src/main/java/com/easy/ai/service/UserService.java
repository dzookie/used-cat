package com.easy.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.ai.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    List<User> findAll();

    User findUserByEmail(String email);

    User findUserByOpenId(String openId);
}
