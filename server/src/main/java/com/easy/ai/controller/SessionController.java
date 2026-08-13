package com.easy.ai.controller;

import com.easy.ai.common.Result;
import com.easy.ai.entity.Session;
import com.easy.ai.dto.SessionAddRequest;
import com.easy.ai.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "会话", description = "会话相关接口")
@RestController
@RequestMapping("/session")
public class SessionController {

    @Resource
    private SessionService sessionService;

    @PostMapping("/addSession")
    @Operation(summary = "新增会话", description = "新增会话")
    public Result addSession(@RequestBody SessionAddRequest body) {
        Session session = new Session();
        session.setUserId(body.getUserId());
        session.setCreateTime(LocalDateTime.now());
        session.setSummary("欢迎使用esay deepseek，你可以告诉我你的问题");
        session.setTitle("新会话" + (int) (Math.random() * 900) + 100);
        boolean flag = sessionService.save(session);
        if (!flag){
            return Result.error("新增会话失败！");
        }
        return Result.success();
    }

    @GetMapping("/deleteSessionById")
    @Operation(summary = "删除会话", description = "通过id删除会话")
    public Result deleteSessionById(@RequestParam("sessionId") Integer sessionId) {
        boolean flag = sessionService.removeById(sessionId);
        return flag ? Result.success("删除成功！", null) : Result.error("删除会话失败！");
    }
}
