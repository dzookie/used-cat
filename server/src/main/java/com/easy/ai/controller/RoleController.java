package com.easy.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.common.Result;
import com.easy.ai.entity.Role;
import com.easy.ai.entity.User;
import com.easy.ai.service.RoleService;
import com.easy.ai.service.UserService;
import com.easy.ai.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/role")
@Tag(name = "后台-角色管理", description = "管理员角色管理相关接口")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @Operation(summary = "分页查询角色列表", description = "获取所有角色，支持分页")
    public Result<PageBean<Role>> getRoleList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {

        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Role::getCreateTime);

        roleService.page(page, wrapper);

        PageBean<Role> pageBean = new PageBean<>(page.getTotal(), page.getRecords());
        return Result.success("查询成功", pageBean);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有角色", description = "获取所有角色列表")
    public Result<List<Role>> getAllRoles() {
        List<Role> list = roleService.list();
        return Result.success("查询成功", list);
    }

    @PostMapping
    @Operation(summary = "新增角色", description = "创建新角色")
    public Result addRole(@RequestBody Role role, @RequestHeader("Authorization") String token) {
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            return Result.error("角色名称不能为空");
        }

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleName, role.getRoleName());
        if (roleService.count(wrapper) > 0) {
            return Result.error("角色名称已存在");
        }

        Claims claims = JwtUtil.parseToken(token);
        User currentUser = userService.findUserByEmail(claims.getSubject());
        role.setCreator(currentUser != null ? currentUser.getNickname() : "未知");
        role.setCreateTime(LocalDateTime.now());
        boolean success = roleService.save(role);
        if (success) {
            return Result.success("新增成功");
        } else {
            return Result.error("新增失败");
        }
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "更新角色", description = "根据角色ID更新角色信息")
    public Result updateRole(@PathVariable("roleId") Integer roleId, @RequestBody Role role) {
        if (roleId == null) {
            return Result.error("角色ID不能为空");
        }
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            return Result.error("角色名称不能为空");
        }

        Role existing = roleService.getById(roleId);
        if (existing == null) {
            return Result.error("角色不存在");
        }

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleName, role.getRoleName());
        wrapper.ne(Role::getRoleId, roleId);
        if (roleService.count(wrapper) > 0) {
            return Result.error("角色名称已存在");
        }

        Role updateRole = new Role();
        updateRole.setRoleId(roleId);
        updateRole.setRoleName(role.getRoleName());
        boolean success = roleService.updateById(updateRole);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "删除角色", description = "根据角色ID删除角色")
    public Result deleteRole(@PathVariable("roleId") Integer roleId) {
        if (roleId == null) {
            return Result.error("角色ID不能为空");
        }

        if (roleId == 1 || roleId == 2) {
            return Result.error("系统默认角色不能删除");
        }

        Role role = roleService.getById(roleId);
        if (role == null) {
            return Result.error("角色不存在");
        }

        boolean success = roleService.removeById(roleId);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}