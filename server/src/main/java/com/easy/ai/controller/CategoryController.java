package com.easy.ai.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.entity.Album;
import com.easy.ai.entity.Category;
import com.easy.ai.entity.Commodity;
import com.easy.ai.entity.CommodityStatus;
import com.easy.ai.common.Result;
import com.easy.ai.service.AlbumService;
import com.easy.ai.service.CategoryService;
import com.easy.ai.service.CommodityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/category")
@Tag(name = "分类", description = "分类相关接口")
public class CategoryController {

  @Autowired
  private CategoryService categoryService;

  @Autowired
  private CommodityService commodityService;

  @Autowired
  private AlbumService albumService;

  @GetMapping("/getCategoryList")
  @Operation(summary = "获取分类列表", description = "获取所有分类列表")
  public Result<List<Category>> getCategoryList() {
    List<Category> list = categoryService.list();

    QueryWrapper<Commodity> queryWrapper = new QueryWrapper<>();
    queryWrapper.in("commodity_type", list.stream().map(Category::getTypeId).collect(Collectors.toList()));
    queryWrapper.eq("status", CommodityStatus.ON_SHELF.getCode());
    List<Commodity> allCommodities = commodityService.list(queryWrapper);

    List<Integer> commodityIds = allCommodities.stream().map(Commodity::getCommodityId).collect(Collectors.toList());
    QueryWrapper<Album> albumQueryWrapper = new QueryWrapper<>();
    albumQueryWrapper.in("commodity_id", commodityIds);
    List<Album> allAlbums = albumService.list(albumQueryWrapper);

    Map<Integer, List<Album>> albumMap = allAlbums.stream()
        .collect(Collectors.groupingBy(Album::getCommodityId));

    for (Commodity commodity : allCommodities) {
      List<Album> albums = albumMap.getOrDefault(commodity.getCommodityId(), Collections.emptyList());
      commodity.setAlbums(albums);
    }

    Map<Integer, List<Commodity>> commodityMap = allCommodities.stream()
        .collect(Collectors.groupingBy(Commodity::getCommodityType));

    for (Category category : list) {
      List<Commodity> recommendations = commodityMap.getOrDefault(category.getTypeId(), Collections.emptyList());
      category.setRecommendationList(recommendations.stream().limit(2).collect(Collectors.toList()));
    }

    return Result.success("获取分类列表成功", list);
  }

  @GetMapping("/admin/list")
  @Operation(summary = "后台分页查询分类", description = "后台管理分页查询分类列表，支持按分类名称搜索")
  public Result<PageBean<Category>> adminList(
      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
      @RequestParam(value = "keyword", required = false) String keyword) {

    Page<Category> page = new Page<>(pageNum, pageSize);
    QueryWrapper<Category> wrapper = new QueryWrapper<>();

    if (keyword != null && !keyword.trim().isEmpty()) {
      wrapper.like("type_name", keyword);
    }

    wrapper.orderByAsc("type_id");

    Page<Category> result = categoryService.page(page, wrapper);
    return Result.success("查询成功", new PageBean<>(result.getTotal(), result.getRecords()));
  }

  @PostMapping("/admin/add")
  @Operation(summary = "后台新增分类", description = "管理员新增商品分类")
  public Result addCategory(@RequestBody Category category) {
    if (category.getTypeName() == null || category.getTypeName().trim().isEmpty()) {
      return Result.error("分类名称不能为空");
    }

    QueryWrapper<Category> wrapper = new QueryWrapper<>();
    wrapper.eq("type_name", category.getTypeName());
    if (categoryService.count(wrapper) > 0) {
      return Result.error("分类名称已存在");
    }

    categoryService.save(category);
    return Result.success("新增成功");
  }

  @PutMapping("/admin/update")
  @Operation(summary = "后台更新分类", description = "管理员更新商品分类")
  public Result updateCategory(@RequestBody Category category) {
    if (category.getTypeId() == null) {
      return Result.error("分类ID不能为空");
    }
    if (category.getTypeName() == null || category.getTypeName().trim().isEmpty()) {
      return Result.error("分类名称不能为空");
    }

    QueryWrapper<Category> wrapper = new QueryWrapper<>();
    wrapper.eq("type_name", category.getTypeName());
    wrapper.ne("type_id", category.getTypeId());
    if (categoryService.count(wrapper) > 0) {
      return Result.error("分类名称已存在");
    }

    categoryService.updateById(category);
    return Result.success("更新成功");
  }

  @DeleteMapping("/admin/delete/{typeId}")
  @Operation(summary = "后台删除分类", description = "管理员删除商品分类")
  public Result deleteCategory(@PathVariable("typeId") Integer typeId) {
    if (typeId == null) {
      return Result.error("分类ID不能为空");
    }

    QueryWrapper<Commodity> commodityWrapper = new QueryWrapper<>();
    commodityWrapper.eq("commodity_type", typeId);
    long count = commodityService.count(commodityWrapper);
    if (count > 0) {
      return Result.error("该分类下还有商品，无法删除");
    }

    categoryService.removeById(typeId);
    return Result.success("删除成功");
  }

}
