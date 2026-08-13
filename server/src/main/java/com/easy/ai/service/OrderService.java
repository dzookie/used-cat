package com.easy.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.ai.entity.Order;

import java.time.LocalDateTime;

public interface OrderService extends IService<Order> {

    Double getTodaySales(LocalDateTime todayStart);
}
