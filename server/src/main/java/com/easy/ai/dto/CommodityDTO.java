package com.easy.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityDTO {
    private Integer userId;
    private String commodityName;
    private String commodityDesc;
    private Integer commodityType;
    private Double price;
    private String brand;
    private String useStatus;
    private String[] images;
}
