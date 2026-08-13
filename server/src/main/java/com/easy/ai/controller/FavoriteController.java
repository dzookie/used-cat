package com.easy.ai.controller;

import com.easy.ai.common.Result;
import com.easy.ai.entity.Commodity;
import com.easy.ai.tools.FavoriteTool;
import com.easy.ai.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
@Tag(name = "收藏商品", description = "收藏商品相关接口")
public class FavoriteController {

    private final FavoriteTool favoriteTool;

    public FavoriteController(FavoriteTool favoriteTool) {
        this.favoriteTool = favoriteTool;
    }

    @PostMapping("/toggle")
    @Operation(summary = "切换收藏状态", description = "收藏/取消收藏商品")
    public Result<Map<String, Object>> toggleFavorite(
            @RequestHeader("Authorization") String token,
            @RequestParam("commodityId") Integer commodityId) {
        Integer userId = getUserIdFromToken(token);
        Map<String, Object> result = favoriteTool.toggle(userId, commodityId);
        return Result.success(null, result);
    }

    @GetMapping("/isFavorited")
    @Operation(summary = "查询是否已收藏", description = "查询当前用户是否已收藏指定商品")
    public Result<Map<String, Object>> isFavorited(
            @RequestHeader("Authorization") String token,
            @RequestParam("commodityId") Integer commodityId) {
        Integer userId = getUserIdFromToken(token);
        boolean favorited = favoriteTool.isFavorited(userId, commodityId);
        return Result.success(null, Map.of("favorited", favorited));
    }

    @GetMapping("/list")
    @Operation(summary = "获取收藏列表", description = "获取当前用户的收藏商品列表")
    public Result<List<Commodity>> getFavoriteList(@RequestHeader("Authorization") String token) {
        Integer userId = getUserIdFromToken(token);
        List<Commodity> commodities = favoriteTool.getFavoriteList(userId);
        if (commodities.isEmpty()) {
            return Result.success("暂无收藏", List.of());
        }
        return Result.success("获取收藏列表成功", commodities);
    }

    @GetMapping("/count")
    @Operation(summary = "获取商品收藏数", description = "获取指定商品的收藏人数")
    public Result<Long> getFavoriteCount(@RequestParam("commodityId") Integer commodityId) {
        long count = favoriteTool.countByCommodityId(commodityId);
        return Result.success("ok", count);
    }

    private Integer getUserIdFromToken(String token) {
        Claims claims = JwtUtil.parseToken(token);
        return claims.get("id", Integer.class);
    }
}
