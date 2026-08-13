package com.easy.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.common.Result;
import com.easy.ai.dto.CommodityDTO;
import com.easy.ai.entity.Album;
import com.easy.ai.entity.Commodity;
import com.easy.ai.service.AlbumService;
import com.easy.ai.service.CommodityService;
import com.easy.ai.tools.CommodityTool;
import com.easy.ai.utils.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/commodity")
@Tag(name = "商品相关", description = "商品相关接口")
public class CommodityController {

    @Resource
    private CommodityTool commodityTool;

    @Resource
    private CommodityService commodityService;

    @Resource
    private AlbumService albumService;

    @GetMapping("/getCommodityById")
    @Operation(summary = "获取商品详情", description = "通过商品ID获取商品详细信息")
    public Result<Commodity> getCommodityById(@RequestParam("commodityId") Integer commodityId) {
        Commodity commodity = commodityTool.getCommodityById(commodityId);
        if (commodity == null) {
            return Result.error("商品不存在");
        }
        return Result.success("获取成功", commodity);
    }

    @GetMapping("/getCommodityList")
    @Operation(summary = "获取商品列表", description = "分页查询商品列表信息")
    public Result<PageBean<Commodity>> getCommodityList(
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam("pageIndex") Integer pageIndex,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "commodityType", required = false) Integer commodityType,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "random", required = false, defaultValue = "false") Boolean random
    ) {
        PageBean<Commodity> pageBean = commodityTool.queryPage(pageIndex, pageSize, userId, commodityType, status, random);
        return Result.success("获取成功", pageBean);
    }

    @GetMapping("/admin/list")
    @Operation(summary = "后台分页查询商品", description = "后台管理分页查询商品列表，支持按商品名称和描述搜索")
    public Result<PageBean<Commodity>> adminList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Page<Commodity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Commodity> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Commodity::getCommodityName, keyword)
                    .or()
                    .like(Commodity::getCommodityDesc, keyword));
        }

        wrapper.orderByDesc(Commodity::getCreateTime);

        Page<Commodity> result = commodityService.page(page, wrapper);
        List<Commodity> records = result.getRecords();

        if (!records.isEmpty()) {
            List<Integer> commodityIds = records.stream()
                    .map(Commodity::getCommodityId)
                    .collect(Collectors.toList());

            List<Album> albums = albumService.lambdaQuery()
                    .in(Album::getCommodityId, commodityIds)
                    .list();

            Map<Integer, List<Album>> albumMap = albums.stream()
                    .collect(Collectors.groupingBy(Album::getCommodityId));

            records.forEach(c -> c.setAlbums(albumMap.getOrDefault(c.getCommodityId(), List.of())));
        }

        return Result.success("查询成功", new PageBean<>(result.getTotal(), records));
    }

    @DeleteMapping("/admin/delete/{commodityId}")
    @Operation(summary = "后台删除商品", description = "管理员删除指定商品")
    public Result<Void> adminDelete(@PathVariable("commodityId") Integer commodityId) {
        if (commodityId == null) {
            return Result.error("商品ID不能为空");
        }
        commodityTool.deleteCommodity(commodityId);
        return Result.success("删除成功");
    }

    @PostMapping("/addCommodity")
    @Operation(summary = "新增商品", description = "新增闲置商品")
    public Result addCommodity(@RequestBody CommodityDTO commodityDTO) {
        if (commodityDTO.getImages() == null || commodityDTO.getImages().length == 0) {
            return Result.error("请上传商品图片");
        }
        commodityTool.addCommodity(commodityDTO);
        return Result.success("商品发布成功");
    }

    @PostMapping("/uploadCommunityImg")
    @Operation(summary = "上传商品图片", description = "上传商品图片到服务器，返回图片路径。")
    public Result<String> uploadCommunityImg(@RequestParam("avatar") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("图片不能为空");
        }
        try {
            String imagePath = FileUploadUtil.uploadFile(file, "sp");
            return Result.success("上传成功", imagePath);
        } catch (IOException e) {
            return Result.error("上传失败");
        }
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "更新商品状态", description = "更新商品的上架/下架/售出状态")
    public Result<Void> updateStatus(@RequestBody Map<String, Integer> params) {
        Integer commodityId = params.get("commodityId");
        Integer status = params.get("status");
        if (commodityId == null || status == null) {
            return Result.error("参数不能为空");
        }
        commodityTool.updateStatus(commodityId, status);
        return Result.success("状态更新成功");
    }

    @PostMapping("/delete")
    @Operation(summary = "删除商品", description = "删除指定商品")
    public Result<Void> deleteCommodity(@RequestBody Map<String, Integer> params) {
        Integer commodityId = params.get("commodityId");
        if (commodityId == null) {
            return Result.error("商品ID不能为空");
        }
        commodityTool.deleteCommodity(commodityId);
        return Result.success("删除成功");
    }

    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键字搜索已上架的商品")
    public Result<List<Commodity>> search(@RequestParam("keyword") String keyword) {
        List<Commodity> records = commodityTool.search(keyword);
        return Result.success("搜索成功", records);
    }

    @PostMapping("/incrementBrowse")
    @Operation(summary = "增加浏览量", description = "商品浏览量+1")
    public Result<Void> incrementBrowse(@RequestBody Map<String, Integer> params) {
        Integer commodityId = params.get("commodityId");
        if (commodityId == null) {
            return Result.error("商品ID不能为空");
        }
        commodityTool.incrementBrowse(commodityId);
        return Result.success("ok");
    }
}
