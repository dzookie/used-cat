package com.easy.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.common.Result;
import com.easy.ai.entity.Order;
import com.easy.ai.entity.User;
import com.easy.ai.mapper.CommodityMapper;
import com.easy.ai.service.CommodityService;
import com.easy.ai.service.OrderService;
import com.easy.ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "后台-数据概览", description = "管理员数据概览统计相关接口")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private CommodityService commodityService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommodityMapper commodityMapper;

    @GetMapping("/stats")
    @Operation(summary = "获取统计数据", description = "获取总用户数、商品总数、订单总数、今日交易金额")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 总用户数 - 只统计普通用户(role=2)
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, 2);
        long totalUsers = userService.count(userWrapper);
        stats.put("totalUsers", totalUsers);

        // 2. 商品总数
        long totalCommodities = commodityService.count();
        stats.put("totalCommodities", totalCommodities);

        // 3. 订单总数
        long totalOrders = orderService.count();
        stats.put("totalOrders", totalOrders);

        // 4. 今日交易金额 - 当天已支付订单金额合计
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Double todaySales = orderService.getTodaySales(todayStart);
        stats.put("todaySales", todaySales != null ? todaySales : 0.0);

        return Result.success("查询成功", stats);
    }

    @GetMapping("/weekOrders")
    @Operation(summary = "获取本周订单", description = "分页查询本周创建的订单列表")
    public Result<PageBean<Order>> getWeekOrders(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = LocalDateTime.of(monday, LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.of(today, LocalTime.MAX);

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreateTime, weekStart)
               .le(Order::getCreateTime, weekEnd)
               .orderByDesc(Order::getCreateTime);

        Page<Order> result = orderService.page(page, wrapper);
        return Result.success("查询成功", new PageBean<>(result.getTotal(), result.getRecords()));
    }

    @GetMapping("/monthCommodities")
    @Operation(summary = "获取本月新增商品", description = "获取本月每天新增商品数量，用于折线图展示")
    public Result<List<Map<String, Object>>> getMonthCommodities() {
        YearMonth yearMonth = YearMonth.now();
        LocalDateTime monthStart = LocalDateTime.of(yearMonth.atDay(1), LocalTime.MIN);
        LocalDateTime monthEnd = LocalDateTime.of(yearMonth.atEndOfMonth(), LocalTime.MAX);

        List<Map<String, Object>> dbData = commodityMapper.selectDailyCountByMonth(monthStart, monthEnd);
        Map<String, Long> dataMap = new HashMap<>();
        for (Map<String, Object> item : dbData) {
            Object dateObj = item.get("date");
            Object countObj = item.get("count");
            if (dateObj != null && countObj != null) {
                String dateStr = dateObj.toString();
                long count = ((Number) countObj).longValue();
                dataMap.put(dateStr, count);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> result = new ArrayList<>();
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            String key = date.toString();
            String label = date.format(formatter);
            long count = dataMap.getOrDefault(key, 0L);

            Map<String, Object> map = new HashMap<>();
            map.put("date", label);
            map.put("count", count);
            result.add(map);
        }

        return Result.success("查询成功", result);
    }
}