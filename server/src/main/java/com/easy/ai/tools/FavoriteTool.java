package com.easy.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easy.ai.entity.Album;
import com.easy.ai.entity.Commodity;
import com.easy.ai.entity.Favorite;
import com.easy.ai.service.AlbumService;
import com.easy.ai.service.CommodityService;
import com.easy.ai.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FavoriteTool {

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private CommodityService commodityService;

    @Resource
    private AlbumService albumService;

    public Map<String, Object> toggle(Integer userId, Integer commodityId) {
        Favorite exist = favoriteService.getOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getCommodityId, commodityId)
        );

        Map<String, Object> result = new HashMap<>();
        if (exist != null) {
            favoriteService.removeById(exist.getId());
            result.put("favorited", false);
            result.put("message", "已取消收藏");
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setCommodityId(commodityId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteService.save(favorite);
            result.put("favorited", true);
            result.put("message", "收藏成功");
        }
        return result;
    }

    public boolean isFavorited(Integer userId, Integer commodityId) {
        return favoriteService.count(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getCommodityId, commodityId)
        ) > 0;
    }

    public long countByCommodityId(Integer commodityId) {
        return favoriteService.count(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getCommodityId, commodityId)
        );
    }

    public List<Commodity> getFavoriteList(Integer userId) {
        List<Favorite> favorites = favoriteService.list(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime)
        );

        List<Integer> commodityIds = favorites.stream()
                .map(Favorite::getCommodityId)
                .collect(Collectors.toList());

        if (commodityIds.isEmpty()) {
            return List.of();
        }

        List<Commodity> commodities = commodityService.listByIds(commodityIds);

        List<Album> albums = albumService.lambdaQuery()
                .in(Album::getCommodityId, commodityIds)
                .list();
        Map<Integer, List<Album>> albumMap = albums.stream()
                .collect(Collectors.groupingBy(Album::getCommodityId));

        commodities.forEach(c ->
                c.setAlbums(albumMap.getOrDefault(c.getCommodityId(), List.of()))
        );

        return commodities;
    }
}
