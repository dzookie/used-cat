package com.easy.ai.tools;

import com.easy.ai.entity.Order;
import com.easy.ai.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderQueryTools {

    @Resource
    private OrderService orderService;

    @Tool(description = "查询当前登录用户的订单列表。可以根据订单状态进行过滤。当用户询问\"我的订单\"、\"订单状态\"时调用此工具。")
    public String getUserOrders(
            @ToolParam(description = "订单状态：0=待付款，1=已付款等待卖家发货，2=已发货运输中，3=已完成。传null或不传则查询所有订单。", required = false)
            Integer status,
            ToolContext toolContext) {

        Integer userId = (Integer) toolContext.getContext().get("userId");
        if (userId == null) {
            return "无法获取当前用户信息，请重新登录后再试。";
        }

        var query = orderService.lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        if (status != null && status >= 0 && status <= 3) {
            query.eq(Order::getStatus, status);
        }

        List<Order> orders = query.list();

        if (orders.isEmpty()) {
            String statusLabel = getStatusLabel(status);
            return "您当前没有" + statusLabel + "订单。";
        }

        StringBuilder sb = new StringBuilder();
        String statusLabel = getStatusLabel(status);
        sb.append("您").append(statusLabel).append("的订单共 ").append(orders.size()).append(" 个：\n\n");

        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            sb.append(String.format("%d. 📦 %s\n", i + 1, o.getCommodityName()));
            sb.append(String.format("   订单编号：%s\n", o.getOrderNo()));
            sb.append(String.format("   金额：¥%.2f × %d = ¥%.2f\n", o.getPrice(), o.getQuantity(), o.getTotalAmount()));
            sb.append(String.format("   状态：%s\n", getStatusText(o.getStatus())));
            sb.append(String.format("   收件人：%s  %s\n", o.getConsignee(), o.getPhone()));
            sb.append(String.format("   地址：%s\n", o.getAddress()));
            if (o.getExpressNo() != null && !o.getExpressNo().isEmpty()) {
                sb.append(String.format("   快递公司：%s  快递单号：%s\n", o.getExpressCompany(), o.getExpressNo()));
            }
            if (o.getPayTime() != null) {
                sb.append(String.format("   付款时间：%s\n", o.getPayTime()));
            }
            sb.append(String.format("   下单时间：%s\n\n", o.getCreateTime()));
        }

        return sb.toString();
    }

    private String getStatusLabel(Integer status) {
        if (status == null || status < 0) {
            return "";
        }
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "已付款";
            case 2 -> "已发货";
            case 3 -> "已完成";
            default -> "";
        };
    }

    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "已付款，等待卖家发货";
            case 2 -> "已发货，运输中";
            case 3 -> "已完成";
            default -> "未知";
        };
    }
}
