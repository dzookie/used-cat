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
@TableName("message")
public class Message {
    @TableId(value = "message_id", type = IdType.AUTO)
    private Integer messageId;
    private Integer conversationId;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private String messageType;
    private Integer isRead;
    private LocalDateTime createTime;
}
