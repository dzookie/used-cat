package com.easy.ai.entity;

import lombok.Getter;

@Getter
public enum CommodityStatus {

    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架"),
    SOLD(2, "售出");

    private final int code;
    private final String desc;

    CommodityStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static Integer codeOf(CommodityStatus status) {
        return status != null ? status.code : null;
    }
}
