package com.easy.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easy.ai.entity.Album;
import com.easy.ai.entity.BrowseHistory;
import com.easy.ai.entity.Commodity;
import com.easy.ai.service.AlbumService;
import com.easy.ai.service.BrowseHistoryService;
import com.easy.ai.service.CommodityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BrowseHistoryTool {

    @Resource
    private BrowseHistoryService browseHistoryService;

    @Resource
    private CommodityService commodityService;

    @Resource
    private AlbumService albumService;

    public void recordBrowse(Integer userId, Integer commodityId) {
        BrowseHistory exist = browseHistoryService.getOne(
                new LambdaQueryWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .eq(BrowseHistory::getCommodityId, commodityId)
        );

        if (exist != null) {
            exist.setBrowseCount(exist.getBrowseCount() + 1);
            exist.setLastBrowseTime(LocalDateTime.now());
            browseHistoryService.updateById(exist);
        } else {
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setCommodityId(commodityId);
            history.setBrowseCount(1);
            history.setLastBrowseTime(LocalDateTime.now());
            browseHistoryService.save(history);
        }
    }

    public List<Commodity> getBrowseHistory(Integer userId) {
        List<BrowseHistory> histories = browseHistoryService.list(
                new LambdaQueryWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .orderByDesc(BrowseHistory::getLastBrowseTime)
        );

        if (histories.isEmpty()) {
            return List.of();
        }

        List<Integer> commodityIds = histories.stream()
                .map(BrowseHistory::getCommodityId)
                .collect(Collectors.toList());

        List<Commodity> commodities = commodityService.listByIds(commodityIds);

        // 按浏览时间排序（listByIds 不保证顺序，需手动重排）
        Map<Integer, Commodity> commodityMap = commodities.stream()
                .collect(Collectors.toMap(Commodity::getCommodityId, c -> c));
        List<Commodity> sortedCommodities = commodityIds.stream()
                .map(commodityMap::get)
                .filter(c -> c != null)
                .collect(Collectors.toList());

        // 附加相册
        List<Album> albums = albumService.lambdaQuery()
                .in(Album::getCommodityId, commodityIds)
                .list();
        Map<Integer, List<Album>> albumMap = albums.stream()
                .collect(Collectors.groupingBy(Album::getCommodityId));

        sortedCommodities.forEach(c -> {
            c.setAlbums(albumMap.getOrDefault(c.getCommodityId(), List.of()));
        });

        return sortedCommodities;
    }
}
