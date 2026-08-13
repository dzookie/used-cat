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
@TableName("conversation")
public class Conversation {
    @TableId(value = "conversation_id", type = IdType.AUTO)
    private Integer conversationId;
    private Integer userId;
    private Integer targetUserId;
    private Integer commodityId;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unreadCount;
    private Integer status;
    private LocalDateTime createTime;
}
