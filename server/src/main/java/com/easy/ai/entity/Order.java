package com.easy.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String orderNo;
    private Integer userId;
    private Integer commodityId;
    private String commodityName;
    private String commodityDesc;
    private String commodityImage;
    private Double price;
    private Integer quantity;
    private Double totalAmount;
    private Integer addressId;
    private String consignee;
    private String phone;
    private String address;
    private Integer status;
    private String payMethod;
    private String tradeNo;
    private String expressNo;
    private String expressCompany;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
