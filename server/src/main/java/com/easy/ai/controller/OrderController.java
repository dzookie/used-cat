package com.easy.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.common.Result;
import com.easy.ai.dto.CreateOrderDTO;
import com.easy.ai.entity.Order;
import com.easy.ai.entity.ReceivingAddress;
import com.easy.ai.service.OrderService;
import com.easy.ai.service.ReceivingAddressService;
import com.easy.ai.tools.OrderTool;
import com.easy.ai.utils.ExpressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/order")
@Tag(name = "订单管理", description = "订单相关接口")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReceivingAddressService receivingAddressService;

    @Autowired
    private ExpressService expressService;

    @Autowired
    private OrderTool orderTool;

    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建新订单")
    public Result<Order> createOrder(@RequestBody CreateOrderDTO dto) {
        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setUserId(dto.getUserId());
        order.setCommodityId(dto.getCommodityId());
        order.setCommodityName(dto.getCommodityName());
        order.setCommodityDesc(dto.getCommodityDesc());
        order.setCommodityImage(dto.getCommodityImage());
        order.setPrice(dto.getPrice());
        order.setQuantity(dto.getQuantity());
        order.setTotalAmount(dto.getTotalAmount());
        order.setAddressId(dto.getAddressId());
        order.setStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        ReceivingAddress address = receivingAddressService.getById(dto.getAddressId());
        if (address != null) {
            order.setConsignee(address.getConsignee());
            order.setPhone(address.getPhone());
            order.setAddress(address.getRegion() + address.getAddress());
        }

        boolean success = orderService.save(order);
        if (success) {
            return Result.success("订单创建成功", order);
        }
        return Result.error("订单创建失败");
    }

    @GetMapping("/list")
    @Operation(summary = "获取订单列表", description = "根据用户id获取订单列表")
    public Result<List<Order>> getOrderList(@Parameter(description = "用户id") @RequestParam("userId") Integer userId) {
        List<Order> orders = orderService.lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime)
                .list();
        return Result.success("获取订单列表成功", orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据订单id获取订单详情")
    public Result<Order> getOrderById(@Parameter(description = "订单id") @PathVariable("id") Integer id) {
        Order order = orderService.getById(id);
        if (order != null) {
            return Result.success("获取订单详情成功", order);
        }
        return Result.error("订单不存在");
    }

    @PutMapping("/updateStatus")
    @Operation(summary = "更新订单状态", description = "更新订单状态")
    public Result<Void> updateOrderStatus(@Parameter(description = "订单id") @RequestParam("id") Integer id,
                                            @Parameter(description = "订单状态") @RequestParam("status") Integer status) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        order.setStatus(status);
        if (status == 1) {
            order.setPayTime(LocalDateTime.now());
        }
        order.setUpdateTime(LocalDateTime.now());

        boolean success = orderService.updateById(order);
        if (success) {
            return Result.success("订单状态更新成功");
        }
        return Result.error("订单状态更新失败");
    }

    @GetMapping("/logistics")
    @Operation(summary = "查询物流信息", description = "根据订单id查询物流轨迹")
    public Result<Map<String, Object>> getLogistics(@Parameter(description = "订单id") @RequestParam("orderId") Integer orderId) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        String expressNo = order.getExpressNo();
        String expressCompany = order.getExpressCompany();
        String phone = order.getPhone();

        if (expressNo == null || expressNo.isEmpty()) {
            return Result.error("暂无物流信息，卖家尚未发货");
        }

        Map<String, Object> logistics = expressService.queryLogistics(expressNo, expressCompany, phone);
        return Result.success("查询成功", logistics);
    }

    @GetMapping("/sellerList")
    @Operation(summary = "获取卖家订单列表", description = "根据卖家用户id获取需要发货的订单列表")
    public Result<List<Order>> getSellerOrderList(@Parameter(description = "卖家用户id") @RequestParam("userId") Integer userId) {
        List<Order> orders = orderTool.getSellerOrders(userId);
        return Result.success("获取成功", orders);
    }

    @PutMapping("/ship")
    @Operation(summary = "发货", description = "卖家填写物流单号进行发货")
    public Result<Void> shipOrder(@RequestBody Map<String, Object> params) {
        Integer orderId = (Integer) params.get("orderId");
        String expressNo = (String) params.get("expressNo");
        String expressCompany = (String) params.get("expressCompany");

        if (orderId == null || expressNo == null || expressNo.isEmpty()
                || expressCompany == null || expressCompany.isEmpty()) {
            return Result.error("参数不完整");
        }

        try {
            orderTool.shipOrder(orderId, expressNo, expressCompany);
            return Result.success("发货成功");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消订单", description = "取消待支付订单，状态改为已取消(3)")
    public Result<Void> cancelOrder(@RequestBody Map<String, Object> params) {
        Integer orderId = (Integer) params.get("orderId");
        Integer userId = (Integer) params.get("userId");

        if (orderId == null || userId == null) {
            return Result.error("参数不完整");
        }

        try {
            orderTool.cancelOrder(orderId, userId);
            return Result.success("订单已取消");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    @Operation(summary = "后台分页查询订单", description = "后台管理分页查询订单列表，支持按订单号和状态搜索")
    public Result<PageBean<Order>> adminList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "status", required = false) Integer status) {

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> result = orderService.page(page, wrapper);
        return Result.success("查询成功", new PageBean<>(result.getTotal(), result.getRecords()));
    }

    @PutMapping("/admin/updateStatus/{id}")
    @Operation(summary = "后台更新订单状态", description = "管理员更新订单状态")
    public Result<Void> adminUpdateStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") Integer status) {
        Order order = orderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        order.setStatus(status);
        if (status == 1) {
            order.setPayTime(LocalDateTime.now());
        }
        order.setUpdateTime(LocalDateTime.now());

        boolean success = orderService.updateById(order);
        if (success) {
            return Result.success("订单状态更新成功");
        }
        return Result.error("订单状态更新失败");
    }
}
