package com.easy.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.dto.CommodityDTO;
import com.easy.ai.entity.Album;
import com.easy.ai.entity.Commodity;
import com.easy.ai.entity.CommodityStatus;
import com.easy.ai.service.AlbumService;
import com.easy.ai.service.CommodityService;
import com.easy.ai.utils.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommodityTool {

    @Resource
    private CommodityService commodityService;

    @Resource
    private AlbumService albumService;

    public Commodity getCommodityById(Integer commodityId) {
        Commodity commodity = commodityService.getById(commodityId);
        if (commodity != null) {
            List<Album> albums = albumService.lambdaQuery()
                    .eq(Album::getCommodityId, commodityId)
                    .list();
            commodity.setAlbums(albums);
        }
        return commodity;
    }

    public PageBean<Commodity> queryPage(Integer pageIndex, Integer pageSize,
                                          Integer userId, Integer commodityType,
                                          Integer status, Boolean random) {
        Page<Commodity> page = new Page<>(pageIndex, pageSize);

        LambdaQueryWrapper<Commodity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Commodity::getUserId, userId);
        wrapper.eq(commodityType != null, Commodity::getCommodityType, commodityType);
        wrapper.eq(status != null, Commodity::getStatus, status);
        if (Boolean.TRUE.equals(random)) {
            wrapper.last("ORDER BY RAND()");
        } else {
            wrapper.orderByDesc(Commodity::getBrowse);
        }

        Page<Commodity> result = commodityService.page(page, wrapper);
        List<Commodity> records = result.getRecords();

        if (!records.isEmpty()) {
            attachAlbums(records);
        }

        return new PageBean<>(result.getTotal(), records);
    }

    public void addCommodity(CommodityDTO dto) {
        Commodity commodity = new Commodity();
        commodity.setUserId(dto.getUserId());
        commodity.setCommodityName(StringUtil.extractFirstLine(dto.getCommodityDesc()));
        commodity.setCommodityDesc(dto.getCommodityDesc());
        commodity.setCommodityType(dto.getCommodityType());
        commodity.setPrice(dto.getPrice());
        commodity.setBrand(dto.getBrand());
        commodity.setUseStatus(dto.getUseStatus());
        commodity.setStatus(CommodityStatus.ON_SHELF.getCode());
        commodity.setBrowse(0);
        commodity.setCreateTime(java.time.LocalDateTime.now());
        commodityService.save(commodity);

        for (String imagePath : dto.getImages()) {
            Album album = new Album();
            album.setCommodityId(commodity.getCommodityId());
            album.setPath(imagePath);
            album.setCreateTime(java.time.LocalDateTime.now());
            albumService.save(album);
        }
    }

    public void updateStatus(Integer commodityId, Integer status) {
        Commodity commodity = commodityService.getById(commodityId);
        if (commodity != null) {
            commodity.setStatus(status);
            commodityService.updateById(commodity);
        }
    }

    public void deleteCommodity(Integer commodityId) {
        commodityService.removeById(commodityId);
        albumService.remove(
                new LambdaQueryWrapper<Album>()
                        .eq(Album::getCommodityId, commodityId)
        );
    }

    public void incrementBrowse(Integer commodityId) {
        Commodity commodity = commodityService.getById(commodityId);
        if (commodity != null) {
            commodity.setBrowse(commodity.getBrowse() == null ? 1 : commodity.getBrowse() + 1);
            commodityService.updateById(commodity);
        }
    }

    public List<Commodity> search(String keyword) {
        LambdaQueryWrapper<Commodity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Commodity::getStatus, CommodityStatus.ON_SHELF.getCode())
                .and(w -> w
                        .like(Commodity::getCommodityName, keyword)
                        .or()
                        .like(Commodity::getCommodityDesc, keyword));

        List<Commodity> records = commodityService.list(wrapper);

        if (!records.isEmpty()) {
            attachAlbums(records);
        }

        return records;
    }

    private void attachAlbums(List<Commodity> commodities) {
        List<Integer> commodityIds = commodities.stream()
                .map(Commodity::getCommodityId)
                .collect(Collectors.toList());

        List<Album> albums = albumService.lambdaQuery()
                .in(Album::getCommodityId, commodityIds)
                .list();

        Map<Integer, List<Album>> albumMap = albums.stream()
                .collect(Collectors.groupingBy(Album::getCommodityId));

        commodities.forEach(c ->
                c.setAlbums(albumMap.getOrDefault(c.getCommodityId(), List.of()))
        );
    }
}
