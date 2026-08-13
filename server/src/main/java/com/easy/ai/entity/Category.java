package com.easy.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;


@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Integer typeId;

    private String typeName;

    private String typeDesc;

    private String img;

    private String color;

    private String en;

    @TableField(exist = false)
    private List<Commodity> recommendationList;
}
