package com.easy.ai.tools;

import com.easy.ai.entity.Commodity;
import com.easy.ai.entity.Order;
import com.easy.ai.service.CommodityService;
import com.easy.ai.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderTool {

    @Resource
    private OrderService orderService;

    @Resource
    private CommodityService commodityService;

    public List<Order> getSellerOrders(Integer userId) {
        List<Integer> commodityIds = commodityService.lambdaQuery()
                .eq(Commodity::getUserId, userId)
                .list()
                .stream()
                .map(Commodity::getCommodityId)
                .collect(Collectors.toList());

        if (commodityIds.isEmpty()) {
            return List.of();
        }

        return orderService.lambdaQuery()
                .in(Order::getCommodityId, commodityIds)
                .eq(Order::getStatus, 1)
                .orderByDesc(Order::getCreateTime)
                .list();
    }

    public void shipOrder(Integer orderId, String expressNo, String expressCompany) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new IllegalStateException("订单状态异常，无法发货");
        }

        order.setExpressNo(expressNo);
        order.setExpressCompany(expressCompany);
        order.setStatus(2);
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);
    }

    public void cancelOrder(Integer orderId, Integer userId) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new IllegalStateException("无权操作该订单");
        }
        if (order.getStatus() != 0) {
            throw new IllegalStateException("只有待支付订单可以取消");
        }

        order.setStatus(3);
        order.setUpdateTime(LocalDateTime.now());
        orderService.updateById(order);
    }
}
