package com.easy.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddReceivingAddressDTO {
    private Integer userId;
    private String consignee;
    private String phone;
    private String region;
    private String address;
}
