package com.easy.ai.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private Integer role;
    private Integer credit;
    private String openId;
    private String provider;
    private LocalDateTime createTime;
}
