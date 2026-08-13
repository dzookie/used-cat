package com.easy.ai.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.easy.ai.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /**
     * WebSocket 处理类
     */
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /**
     * 用户会话映射
     */
    private final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    /**
     * JSON 映射器
     */
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * WebSocket 连接建立时的处理逻辑
     * @param session WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("WebSocket 用户上线: userId={}, 当前在线: {}", userId, userSessions.size());
        }
    }
    /**
     * WebSocket 连接关闭时的处理逻辑
     * @param session WebSocket 会话
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = (Integer) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket 用户下线: userId={}, 当前在线: {}", userId, userSessions.size());
        }
    }
    /**
     * 向指定用户发送消息
     * @param userId 用户 ID
     * @param message 要发送的消息
     */
    public void sendToUser(Integer userId, Message message) {
        WebSocketSession session = userSessions.get(userId);
        if (session == null || !session.isOpen()) {
            log.warn("WebSocket 推送失败: 用户 {} 不在线或连接已关闭, 消息: {}", userId, message.getContent());
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
            log.info("WebSocket 推送成功: userId={}, content={}", userId, message.getContent());
        } catch (Exception e) {
            log.error("WebSocket 推送异常: userId={}, error={}", userId, e.getMessage());
        }
    }
}
