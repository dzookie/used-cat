package com.easy.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("receiving_address")
public class ReceivingAddress {
  @TableId(type = IdType.AUTO)
  private Integer id;
  private Integer userId;
  private String consignee;
  private String phone;
  private String region;
  private String address;
}
