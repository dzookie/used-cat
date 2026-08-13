package com.easy.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.models.security.SecurityScheme.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("commodity")
public class Commodity {
    @TableId(type = IdType.AUTO)
    private Integer commodityId;
    private Integer userId;
    // 商品状态 0：下架，1：上架, 2:售出 @see CommodityStatus
    private Integer status;
    private String commodityName;
    private String commodityDesc;
    private String brand;
    private Integer quality;
    private String useStatus;
    private double price;
    private Integer commodityType;
    private Integer browse;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<Album> albums;
}
