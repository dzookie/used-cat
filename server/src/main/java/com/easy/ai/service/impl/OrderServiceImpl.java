package com.easy.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.ai.entity.Order;
import com.easy.ai.mapper.OrderMapper;
import com.easy.ai.service.OrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public Double getTodaySales(LocalDateTime todayStart) {
        return baseMapper.selectTodaySales(todayStart);
    }
}
