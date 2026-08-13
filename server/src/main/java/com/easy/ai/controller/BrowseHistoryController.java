package com.easy.ai.controller;

import com.easy.ai.common.Result;
import com.easy.ai.entity.Commodity;
import com.easy.ai.tools.BrowseHistoryTool;
import com.easy.ai.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/browseHistory")
@Tag(name = "浏览记录", description = "用户浏览记录相关接口")
public class BrowseHistoryController {

    @Resource
    private BrowseHistoryTool browseHistoryTool;

    @PostMapping("/record")
    @Operation(summary = "记录浏览", description = "记录或更新用户对商品的浏览")
    public Result<Void> recordBrowse(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Integer> params) {
        Integer userId = getUserIdFromToken(token);
        Integer commodityId = params.get("commodityId");
        if (commodityId == null) {
            return Result.error("商品ID不能为空");
        }
        browseHistoryTool.recordBrowse(userId, commodityId);
        return Result.success("ok");
    }

    @GetMapping("/list")
    @Operation(summary = "获取浏览记录列表", description = "获取当前用户的浏览记录商品列表")
    public Result<List<Commodity>> getBrowseHistoryList(@RequestHeader("Authorization") String token) {
        Integer userId = getUserIdFromToken(token);
        List<Commodity> list = browseHistoryTool.getBrowseHistory(userId);
        if (list.isEmpty()) {
            return Result.success("暂无浏览记录", List.of());
        }
        return Result.success("获取浏览记录成功", list);
    }

    private Integer getUserIdFromToken(String token) {
        Claims claims = JwtUtil.parseToken(token);
        return claims.get("id", Integer.class);
    }
}
