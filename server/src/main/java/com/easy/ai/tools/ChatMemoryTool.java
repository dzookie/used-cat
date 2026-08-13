package com.easy.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.easy.ai.entity.History;
import com.easy.ai.entity.Session;
import com.easy.ai.service.HistoryService;
import com.easy.ai.service.SessionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ChatMemoryTool {

    @Resource
    private SessionService sessionService;

    @Resource
    private HistoryService historyService;

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    /**
     * 获取或创建会话
     * @param userId
     * @return
     */
    public Session getOrCreateSession(Integer userId) {
        Session session = sessionService.getOne(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getUserId, userId)
        );

        if (session == null) {
            session = new Session();
            session.setUserId(userId);
            session.setCreateTime(LocalDateTime.now());
            session.setTitle("AI智能客服");
            session.setSummary("你好！我是二手猫智能助手，有什么可以帮你的吗？");
            sessionService.save(session);
        }

        return session;
    }

    public Session getSession(Long sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessionService.getOne(
                new LambdaQueryWrapper<Session>()
                        .eq(Session::getSessionId, sessionId)
        );
    }

    /**
     * 加载会话历史记录
     * @return
     */
    public List<History> loadHistory(Long sessionId) {
        return historyService.list(
                new LambdaUpdateWrapper<History>()
                        .eq(History::getSessionId, sessionId)
        );
    }

    /**
     * 保存用户消息
     * @param message
     * @param sessionId
     * @return
     */
    public History saveUserMessage(String message, Long sessionId) {
        History history = new History();
        history.setDatetime(LocalDateTime.now());
        history.setRole(ROLE_USER);
        history.setContent(message);
        history.setSessionId(sessionId);
        historyService.save(history);
        return history;
    }

    /**
     * 保存助手消息
     * @param content
     * @param sessionId
     */
    public void saveAssistantMessage(String content, Long sessionId) {
        History history = new History();
        history.setDatetime(LocalDateTime.now());
        history.setRole(ROLE_ASSISTANT);
        history.setContent(content);
        history.setSessionId(sessionId);
        historyService.save(history);
    }

    /**
     * 加载最近历史记录
     * @param sessionId
     * @param excludeId
     * @return
     */
    public List<History> loadRecentHistory(Long sessionId, Long excludeId) {
        return historyService.list(
                new LambdaUpdateWrapper<History>()
                        .eq(History::getSessionId, sessionId)
                        .ne(History::getId, excludeId)
        );
    }
}
