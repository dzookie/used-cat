package com.easy.ai.utils;

import com.alipay.api.AlipayApiException;
import com.easy.ai.config.AlipayConfig;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGateway(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "JSON",
                "UTF-8",
                alipayConfig.getAlipayPublicKey(),
                "RSA2"
        );
    }

    /**
     * 创建支付宝二维码支付
     * @param outTradeNo 订单号
     * @param totalAmount 订单金额
     * @param subject 订单标题
     * @return 二维码图片
     */
    public String createQrCodePayment(String outTradeNo, String totalAmount, String subject) {
        try {
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            Map<String, String> bizMap = new HashMap<>();
            bizMap.put("out_trade_no", outTradeNo);
            bizMap.put("total_amount", totalAmount);
            bizMap.put("subject", subject);
            request.setBizContent(objectMapper.writeValueAsString(bizMap));
            AlipayClient alipayClient = getAlipayClient();
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("二维码创建成功, outTradeNo: {}, qrCode: {}", outTradeNo, response.getQrCode());
                return response.getQrCode();
            } else {
                log.error("二维码创建失败: {}, subCode: {}, subMsg: {}",
                        response.getMsg(), response.getSubCode(), response.getSubMsg());
                return null;
            }
        } catch (AlipayApiException e) {
            log.error("二维码支付异常: {}", e.getMessage(), e);
            return null;
        } catch (JsonProcessingException e) {
            log.error("JSON序列化异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证支付宝回调签名
     * @param params 回调参数
     * @return 是否验证通过
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2"
            );
        } catch (AlipayApiException e) {
            log.error("支付宝验签异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 查询支付宝支付状态
     * @param outTradeNo 订单号
     * @return 支付状态
     */
    public String queryPaymentStatus(String outTradeNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            Map<String, String> bizMap = new HashMap<>();
            bizMap.put("out_trade_no", outTradeNo);
            request.setBizContent(objectMapper.writeValueAsString(bizMap));

            AlipayClient alipayClient = getAlipayClient();
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess() && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
                return "SUCCESS";
            } else if ("TRADE_CLOSED".equals(response.getTradeStatus())) {
                return "CLOSED";
            } else if ("WAIT_BUYER_PAY".equals(response.getTradeStatus())) {
                return "WAITING";
            }
            return "UNKNOWN";
        } catch (AlipayApiException e) {
            log.error("支付宝查询异常: {}", e.getMessage(), e);
            return "ERROR";
        } catch (JsonProcessingException e) {
            log.error("JSON序列化异常: {}", e.getMessage(), e);
            return "ERROR";
        }
    }
}
