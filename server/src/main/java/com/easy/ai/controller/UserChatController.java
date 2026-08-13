package com.easy.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.easy.ai.common.Result;
import com.easy.ai.entity.Conversation;
import com.easy.ai.entity.Message;
import com.easy.ai.entity.User;
import com.easy.ai.service.ConversationService;
import com.easy.ai.service.MessageService;
import com.easy.ai.service.UserService;
import com.easy.ai.utils.FileUploadUtil;
import com.easy.ai.utils.JwtUtil;
import com.easy.ai.websocket.ChatWebSocketHandler;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@Tag(name = "用户聊天", description = "用户之间聊天相关接口")
public class UserChatController {

    @Resource
    private ConversationService conversationService;
    @Resource
    private MessageService messageService;
    @Resource
    private UserService userService;
    @Resource
    private ChatWebSocketHandler webSocketHandler;

    private Integer getUserIdFromToken(String token) {
        Claims claims = JwtUtil.parseToken(token);
        return claims.get("id", Integer.class);
    }

    @GetMapping("/conversation/list")
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有聊天会话")
    public Result getConversationList(@RequestHeader("Authorization") String token) {
        Integer userId = getUserIdFromToken(token);

        List<Conversation> conversations = conversationService.list(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getStatus, 1)
                        .orderByDesc(Conversation::getLastTime)
        );

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Conversation conv : conversations) {
            User targetUser = userService.getById(conv.getTargetUserId());

            Map<String, Object> item = new HashMap<>();
            item.put("id", conv.getConversationId());
            item.put("title", targetUser != null ? targetUser.getNickname() : "未知用户");
            item.put("content", conv.getLastMessage());
            item.put("time", conv.getLastTime());
            item.put("avatar", targetUser != null ? targetUser.getAvatar() : null);
            item.put("unread", conv.getUnreadCount() > 0);
            item.put("unreadCount", conv.getUnreadCount());
            item.put("targetUserId", conv.getTargetUserId());
            item.put("commodityId", conv.getCommodityId());
            resultList.add(item);
        }

        return Result.success("获取会话列表成功", resultList);
    }

    @GetMapping("/message/list")
    @Operation(summary = "获取消息列表", description = "获取指定会话的所有消息（含对方发送的）")
    public Result getMessageList(@RequestParam("conversationId") Integer conversationId) {
        Conversation conv = conversationService.getById(conversationId);
        List<Integer> convIds = new ArrayList<>();
        convIds.add(conversationId);

        if (conv != null) {
            Conversation partnerConv = conversationService.getOne(
                    new LambdaQueryWrapper<Conversation>()
                            .eq(Conversation::getUserId, conv.getTargetUserId())
                            .eq(Conversation::getTargetUserId, conv.getUserId())
            );
            if (partnerConv != null) {
                convIds.add(partnerConv.getConversationId());
            }
        }

        List<Message> messages = messageService.list(
                new LambdaQueryWrapper<Message>()
                        .in(Message::getConversationId, convIds)
                        .orderByAsc(Message::getCreateTime)
        );
        return Result.success("获取消息列表成功", messages);
    }

    @PostMapping("/conversation/create")
    @Operation(summary = "创建或获取会话", description = "创建用户之间的聊天会话，如果已存在则返回已有会话")
    public Result createConversation(
            @RequestHeader("Authorization") String token,
            @RequestParam("targetUserId") Integer targetUserId,
            @RequestParam(value = "commodityId", required = false) Integer commodityId) {
        Integer userId = getUserIdFromToken(token);

        Conversation existConv = conversationService.getOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .eq(Conversation::getTargetUserId, targetUserId)
        );

        if (existConv != null) {
            if (existConv.getStatus() != null && existConv.getStatus() == 0) {
                conversationService.update(
                        new LambdaUpdateWrapper<Conversation>()
                                .eq(Conversation::getConversationId, existConv.getConversationId())
                                .set(Conversation::getStatus, 1)
                );
                existConv.setStatus(1);
            }
            return Result.success("会话已存在", existConv);
        }

        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTargetUserId(targetUserId);
        conversation.setCommodityId(commodityId);
        conversation.setUnreadCount(0);
        conversation.setStatus(1);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setLastTime(LocalDateTime.now());
        conversation.setLastMessage("");
        conversationService.save(conversation);

        Conversation reverseConv = conversationService.getOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, targetUserId)
                        .eq(Conversation::getTargetUserId, userId)
        );

        if (reverseConv == null) {
            Conversation reverse = new Conversation();
            reverse.setUserId(targetUserId);
            reverse.setTargetUserId(userId);
            reverse.setCommodityId(commodityId);
            reverse.setUnreadCount(0);
            reverse.setStatus(1);
            reverse.setCreateTime(LocalDateTime.now());
            reverse.setLastTime(LocalDateTime.now());
            reverse.setLastMessage("");
            conversationService.save(reverse);
        } else if (reverseConv.getStatus() != null && reverseConv.getStatus() == 0) {
            conversationService.update(
                    new LambdaUpdateWrapper<Conversation>()
                            .eq(Conversation::getConversationId, reverseConv.getConversationId())
                            .set(Conversation::getStatus, 1)
            );
        }

        return Result.success("创建会话成功", conversation);
    }

    @PostMapping("/message/send")
    @Operation(summary = "发送消息", description = "发送聊天消息")
    public Result sendMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {
        Integer senderId = getUserIdFromToken(token);
        Integer conversationId = (Integer) body.get("conversationId");
        Integer receiverId = (Integer) body.get("receiverId");
        String content = (String) body.get("content");
        String messageType = body.getOrDefault("messageType", "text").toString();

        String lastMessage = "image".equals(messageType) ? "[图片]" : content;

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageService.save(message);

        conversationService.update(
                new LambdaUpdateWrapper<Conversation>()
                        .eq(Conversation::getConversationId, conversationId)
                        .set(Conversation::getLastMessage, lastMessage)
                        .set(Conversation::getLastTime, LocalDateTime.now())
        );

        Conversation receiverConv = conversationService.getOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, receiverId)
                        .eq(Conversation::getTargetUserId, senderId)
        );
        if (receiverConv != null) {
            if (receiverConv.getStatus() != null && receiverConv.getStatus() == 0) {
                conversationService.update(
                        new LambdaUpdateWrapper<Conversation>()
                                .eq(Conversation::getConversationId, receiverConv.getConversationId())
                                .set(Conversation::getStatus, 1)
                );
            }
            conversationService.update(
                    new LambdaUpdateWrapper<Conversation>()
                            .eq(Conversation::getConversationId, receiverConv.getConversationId())
                            .set(Conversation::getLastMessage, lastMessage)
                            .set(Conversation::getLastTime, LocalDateTime.now())
                            .setSql("unread_count = unread_count + 1")
            );
        }
        webSocketHandler.sendToUser(senderId, message);
        if (receiverConv != null) {
            Message receiverMsg = new Message();
            receiverMsg.setMessageId(message.getMessageId());
            receiverMsg.setConversationId(receiverConv.getConversationId());
            receiverMsg.setSenderId(senderId);
            receiverMsg.setReceiverId(receiverId);
            receiverMsg.setContent(content);
            receiverMsg.setMessageType(messageType);
            receiverMsg.setIsRead(0);
            receiverMsg.setCreateTime(message.getCreateTime());
            webSocketHandler.sendToUser(receiverId, receiverMsg);
        }
        return Result.success("发送成功", message);
    }

    @GetMapping("/conversation/read")
    @Operation(summary = "标记会话已读", description = "将指定会话的未读消息标记为已读")
    public Result markAsRead(
            @RequestHeader("Authorization") String token,
            @RequestParam("conversationId") Integer conversationId) {
        Integer userId = getUserIdFromToken(token);

        Conversation conversation = conversationService.getById(conversationId);
        if (conversation != null && conversation.getUserId().equals(userId)) {
            conversation.setUnreadCount(0);
            conversationService.updateById(conversation);
        }

        return Result.success("标记已读成功");
    }

    @PostMapping("/conversation/delete")
    @Operation(summary = "删除会话", description = "软删除会话（status=0），对方不受影响，聊天记录保留")
    public Result deleteConversation(
            @RequestHeader("Authorization") String token,
            @RequestParam("conversationId") Integer conversationId) {
        Integer userId = getUserIdFromToken(token);

        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            return Result.error("无权操作该会话");
        }

        conversationService.update(
                new LambdaUpdateWrapper<Conversation>()
                        .eq(Conversation::getConversationId, conversationId)
                        .set(Conversation::getStatus, 0)
        );

        return Result.success("删除成功");
    }

    @PostMapping("/upload")
    @Operation(summary = "上传聊天图片", description = "上传聊天图片到服务器，返回图片路径")
    public Result<String> uploadChatImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("图片不能为空");
        }
        try {
            String imagePath = FileUploadUtil.uploadFile(file, "chat");
            return Result.success("上传成功", imagePath);
        } catch (IOException e) {
            return Result.error("上传失败");
        }
    }
}
