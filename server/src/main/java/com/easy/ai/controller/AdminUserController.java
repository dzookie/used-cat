package com.easy.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.easy.ai.common.PageBean;
import com.easy.ai.common.Result;
import com.easy.ai.entity.Role;
import com.easy.ai.entity.User;
import com.easy.ai.service.RoleService;
import com.easy.ai.service.UserService;
import com.easy.ai.utils.PasswordUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "后台-用户管理", description = "管理员用户管理相关接口")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/roles")
    @Operation(summary = "获取角色列表", description = "获取所有角色")
    public Result<List<Role>> getRoleList() {
        List<Role> list = roleService.list();
        return Result.success("查询成功", list);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "获取系统所有用户，支持分页、关键字搜索和角色筛选")
    public Result<PageBean<User>> getUserList(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) Integer role) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }

        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        wrapper.orderByDesc(User::getCreateTime);

        userService.page(page, wrapper);

        List<User> records = page.getRecords();
        for (User user : records) {
            user.setPassword(null);
        }

        PageBean<User> pageBean = new PageBean<>(page.getTotal(), records);
        return Result.success("查询成功", pageBean);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public Result deleteUser(@PathVariable("userId") Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getRole() != null && user.getRole() == 1) {
            return Result.error("不能删除管理员账户");
        }
        boolean success = userService.removeById(userId);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @PutMapping("/resetPassword/{userId}")
    @Operation(summary = "重置用户密码", description = "将用户密码重置为 usedcat")
    public Result resetPassword(@PathVariable("userId") Integer userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        String newPassword = PasswordUtil.encode("usedcat");
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setPassword(newPassword);
        boolean success = userService.updateById(updateUser);
        if (success) {
            return Result.success("密码已重置为 usedcat");
        } else {
            return Result.error("重置失败");
        }
    }
}
