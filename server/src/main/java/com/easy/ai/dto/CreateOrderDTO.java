package com.easy.ai.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Integer userId;
    private Integer commodityId;
    private String commodityName;
    private String commodityDesc;
    private String commodityImage;
    private Double price;
    private Integer quantity;
    private Double totalAmount;
    private Integer addressId;
}
