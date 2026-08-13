package com.easy.ai.controller;

import com.easy.ai.agent.UsedCatAgent;
import com.easy.ai.entity.History;
import com.easy.ai.common.Result;
import com.easy.ai.entity.Session;
import com.easy.ai.tools.ChatMemoryTool;
import com.easy.ai.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/ai")
@Tag(name = "ai对话", description = "大模型对话相关接口")
public class ChatController {

    @Resource
    private UsedCatAgent usedCatAgent;

    @Resource
    private ChatMemoryTool chatMemoryTool;

    private Integer getUserIdFromToken(String token) {
        Claims claims = JwtUtil.parseToken(token);
        return claims.get("id", Integer.class);
    }

    @GetMapping("/getCurrSessionHistoryList")
    @Operation(summary = "获取历史记录列表", description = "通过会话id获取当前会话历史记录")
    public Result getCurrSessionHistoryList(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "sessionId") Long sessionId) {
        Integer userId = getUserIdFromToken(token);
        Session session = chatMemoryTool.getSession(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            return Result.error("无权访问该会话");
        }
        List<History> histories = chatMemoryTool.loadHistory(sessionId);
        return Result.success("获取历史记录成功", histories);
    }

    @GetMapping("/customerServiceInit")
    @Operation(summary = "初始化客服会话", description = "查找该用户客服会话，不存在则创建")
    public Result<Session> customerServiceInit(@RequestHeader("Authorization") String token) {
        Integer userId = getUserIdFromToken(token);
        Session session = chatMemoryTool.getOrCreateSession(userId);
        return Result.success("获取会话成功", session);
    }

    @GetMapping(value = "/customerServiceChat", produces = "text/stream;charset=utf-8")
    @Operation(summary = "客服流式对话", description = "智能客服流式对话，自动按userId查找或创建会话")
    public Flux<String> customerServiceChat(
            @RequestParam("message") String message,
            @RequestHeader("Authorization") String token
    ) {
        Integer userId = getUserIdFromToken(token);
        return usedCatAgent.streamChat(message, userId);
    }

    @PostMapping("/beautifyDescription")
    @Operation(summary = "美化商品描述", description = "使用AI优化商品描述文案，使其更具吸引力")
    public Result<String> beautifyDescription(@RequestParam("text") String text) {
        String beautified = usedCatAgent.beautifyDescription(text);
        return Result.success("美化成功", beautified);
    }


}
