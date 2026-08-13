package com.easy.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("session")
public class Session {
    @TableId(value = "session_id", type = IdType.AUTO)
    private Integer sessionId;
    private Integer userId;
    private String summary;
    private String title;
    private Integer isTop;
    private LocalDateTime createTime;
}
