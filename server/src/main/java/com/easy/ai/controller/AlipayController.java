package com.easy.ai.controller;

import com.easy.ai.common.Result;
import com.easy.ai.entity.Commodity;
import com.easy.ai.entity.CommodityStatus;
import com.easy.ai.entity.Order;
import com.easy.ai.service.CommodityService;
import com.easy.ai.service.OrderService;
import com.easy.ai.utils.AlipayService;
import com.easy.ai.utils.StringUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/alipay")
@Tag(name = "支付宝支付", description = "支付宝支付相关接口")
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommodityService commodityService;

    @GetMapping("/precreate")
    @Operation(summary = "发起二维码支付", description = "生成支付宝支付二维码")
    public Result<String> payQrCode(
            @Parameter(description = "商户订单号") @RequestParam("outTradeNo") String outTradeNo,
            @Parameter(description = "支付金额") @RequestParam("totalAmount") String totalAmount,
            @Parameter(description = "商品标题") @RequestParam("subject") String subject
    ) {
        String subjectShort = StringUtil.extractFirstLine(subject);
        log.info("发起二维码支付请求, outTradeNo: {}, totalAmount: {}, subject: {}", outTradeNo, totalAmount, subjectShort);
        String qrCode = alipayService.createQrCodePayment(outTradeNo, totalAmount, subjectShort);
        if (qrCode != null) {
            return Result.success("二维码生成成功", qrCode);
        }
        return Result.error("二维码生成失败");
    }

    @PostMapping("/notify")
    @Operation(summary = "支付回调", description = "支付宝异步通知回调")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        log.info("收到支付宝回调, params: {}", params);
        if (alipayService.verifySignature(params)) {
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                log.info("支付成功, outTradeNo: {}, tradeNo: {}, totalAmount: {}", outTradeNo, tradeNo, totalAmount);
                Order order = orderService.lambdaQuery()
                        .eq(Order::getOrderNo, outTradeNo)
                        .one();
                if (order != null) {
                    if (order.getStatus() != null && order.getStatus() != 1) {
                        order.setStatus(1);
                        order.setPayMethod("alipay");
                        order.setTradeNo(tradeNo);
                        order.setPayTime(LocalDateTime.now());
                        order.setUpdateTime(LocalDateTime.now());
                        boolean updated = orderService.updateById(order);
                        log.info("订单状态更新, outTradeNo: {}, updated: {}", outTradeNo, updated);
                        Commodity commodity = commodityService.getById(order.getCommodityId());
                        if (commodity != null && !Integer.valueOf(CommodityStatus.SOLD.getCode()).equals(commodity.getStatus())) {
                            commodity.setStatus(CommodityStatus.SOLD.getCode());
                            commodityService.updateById(commodity);
                            log.info("商品状态更新为已售出, commodityId: {}", commodity.getCommodityId());
                        }
                    } else {
                        log.info("订单已支付或状态异常, outTradeNo: {}, status: {}", outTradeNo, order.getStatus());
                    }
                } else {
                    log.warn("未找到对应订单, outTradeNo: {}", outTradeNo);
                }
                return "success";
            }
        }
        return "fail";
    }

    @GetMapping("/return")
    @Operation(summary = "支付返回", description = "支付宝同步通知回调")
    public Result<Map<String, String>> returnView(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        log.info("收到支付宝同步返回, params: {}", params);

        if (alipayService.verifySignature(params)) {
            Map<String, String> result = new HashMap<>();
            result.put("outTradeNo", params.get("out_trade_no"));
            result.put("tradeNo", params.get("trade_no"));
            result.put("tradeStatus", params.get("trade_status"));
            return Result.success("支付成功", result);
        }
        return Result.error("验签失败");
    }

    @GetMapping("/query")
    @Operation(summary = "查询支付状态", description = "查询订单支付状态")
    public Result<String> query(@Parameter(description = "商户订单号") @RequestParam("outTradeNo") String outTradeNo) {
        log.info("查询支付状态, outTradeNo: {}", outTradeNo);
        String status = alipayService.queryPaymentStatus(outTradeNo);

        if ("SUCCESS".equals(status)) {
            Order order = orderService.lambdaQuery()
                    .eq(Order::getOrderNo, outTradeNo)
                    .one();
            if (order != null && order.getStatus() != null && order.getStatus() != 1) {
                order.setStatus(1);
                order.setPayMethod("alipay");
                order.setPayTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                orderService.updateById(order);
                log.info("轮询查询后自动更新订单状态, outTradeNo: {}", outTradeNo);

                Commodity commodity = commodityService.getById(order.getCommodityId());
                if (commodity != null && !Integer.valueOf(CommodityStatus.SOLD.getCode()).equals(commodity.getStatus())) {
                    commodity.setStatus(CommodityStatus.SOLD.getCode());
                    commodityService.updateById(commodity);
                    log.info("轮询查询后自动更新商品状态为已售出, commodityId: {}", commodity.getCommodityId());
                }
            }
        }

        return Result.success("查询成功", status);
    }
}
