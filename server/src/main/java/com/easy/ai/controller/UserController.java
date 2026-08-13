package com.easy.ai.controller;

import com.easy.ai.common.Result;
import com.easy.ai.entity.User;
import com.easy.ai.dto.RegisterDTO;
import com.easy.ai.dto.ResetPasswordDTO;
import com.easy.ai.dto.UpdateUserDTO;
import com.easy.ai.dto.UserDTO;
import com.easy.ai.service.UserService;
import com.easy.ai.utils.FileUploadUtil;
import com.easy.ai.utils.JwtUtil;
import com.easy.ai.utils.MailUtil;
import com.easy.ai.utils.PasswordUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Tag(name = "用户信息", description = "用户相关接口")
@Validated
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MailUtil mailUtil;

    @GetMapping("/sendCode")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码并存入Redis，5分钟有效")
    public Result sendVerificationCode(@RequestParam("email") String email) {
        User existUser = userService.findUserByEmail(email);
        if (existUser != null) {
            return Result.error("该邮箱已被注册");
        }

        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        String redisKey = "verify:code:" + email;
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        mailUtil.sendVerificationCode(email, code);

        return Result.success("验证码已发送请查收邮件,验证码5分钟内有效，请勿泄露他人。", null);
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "用户登录接口")
    public Result login(@Valid @RequestBody UserDTO user) {
        String email = user.getEmail();
        String password = user.getPassword();

        User findUser = userService.findUserByEmail(email);

        if (findUser == null) {
            return Result.error("用户不存在");
        }

        if (PasswordUtil.matches(password, findUser.getPassword())) {
            String token = JwtUtil.getToken(findUser);
            return Result.success("登录成功", "Bearer " + token);
        } else {
            return Result.error("密码错误");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "注册", description = "用户注册接口")
    public Result register(@Valid @RequestBody RegisterDTO registerDTO) {
        String email = registerDTO.getEmail();
        String code = registerDTO.getCode();
        String password = registerDTO.getPassword();

        User existUser = userService.findUserByEmail(email);
        if (existUser != null) {
            return Result.error("该邮箱已被注册");
        }

        String redisKey = "verify:code:" + email;
        String redisCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisCode == null) {
            return Result.error("验证码已过期，请重新获取");
        }
        if (!redisCode.equals(code)) {
            return Result.error("验证码错误");
        }

        String encodedPassword = PasswordUtil.encode(password);
        String randomSuffix = String.valueOf((int) (Math.random() * 9000 + 1000));
        String nickname = "二手猫" + randomSuffix;

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setNickname(nickname);
        user.setAvatar("/upload/avatar/a339555f1baa49a6ac58330b089d4984.jpeg");
        user.setRole(2);
        user.setCredit(0);
        user.setCreateTime(LocalDateTime.now());

        boolean success = userService.save(user);
        if (success) {
            stringRedisTemplate.delete(redisKey);
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败，请稍后重试");
        }
    }

    @GetMapping("/getCurrUser")
    @Operation(summary = "获取当前登录用户信息", description = "通过token获取当前登录用户信息")
    public Result getCurrUserByToken(@RequestHeader("Authorization") String token) {
        try {
            Claims claims = JwtUtil.parseToken(token);
            User findUser = userService.findUserByEmail(claims.getSubject());
            if (findUser == null) {
                return Result.error("用户不存在");
            }
            findUser.setPassword(null);
            return Result.success("获取用户信息成功！", findUser);
        } catch (Exception e) {
            return Result.error("token无效或已过期");
        }
    }

    @GetMapping("/getUserByUserId")
    @Operation(summary = "获取商品发布者用户信息", description = "根据该商品发布用户id获取该用户信息")
    public Result<User> getUserByCommodityId(@RequestParam("userId") Integer userId) {
        User findUser = userService.getById(userId);
        if(findUser == null) {
            return Result.error("用户不存在");
        }
        findUser.setPassword(null);
        return Result.success("获取用户信息成功！", findUser);
    }

    @PostMapping("/updateUser")
    @Operation(summary = "更新用户信息", description = "根据用户id更新用户信息")
    public Result updateUser(@RequestBody UpdateUserDTO updateUserDTO) {
        Integer userId = updateUserDTO.getUserId();
        String nickname = updateUserDTO.getNickname();

        User existingUser = userService.getById(userId);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        User user = new User();
        user.setUserId(userId);
        user.setNickname(nickname);
        boolean success = userService.updateById(user);

        if (success) {
            return Result.success("更新用户信息成功！", null);
        } else {
            return Result.error("更新用户信息失败");
        }
    }

    @PostMapping("/updateAvatar")
    @Operation(summary = "更新用户头像", description = "根据用户id更新用户头像")
    public Result updateAvatar(@RequestParam("userId") Integer userId, @RequestParam("avatar") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("头像文件不能为空");
        }

        User existingUser = userService.getById(userId);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        String avatarPath;
        try {
            avatarPath = FileUploadUtil.uploadFile(file, "avatar");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("头像上传失败");
        }

        User user = new User();
        user.setUserId(userId);
        user.setAvatar(avatarPath);
        boolean success = userService.updateById(user);

        if (success) {
            return Result.success("头像更新成功！", avatarPath);
        } else {
            return Result.error("头像更新失败");
        }
    }

    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码", description = "验证旧密码后更新为新密码")
    public Result resetPassword(@RequestHeader("Authorization") String token,
                                @Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        Claims claims = JwtUtil.parseToken(token);
        String email = claims.getSubject();

        User findUser = userService.findUserByEmail(email);
        if (findUser == null) {
            return Result.error("用户不存在");
        }

        if (!PasswordUtil.matches(resetPasswordDTO.getOldPassword(), findUser.getPassword())) {
            return Result.error("旧密码错误");
        }

        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }

        String encodedPassword = PasswordUtil.encode(resetPasswordDTO.getNewPassword());

        User user = new User();
        user.setUserId(findUser.getUserId());
        user.setPassword(encodedPassword);
        boolean success = userService.updateById(user);

        if (success) {
            return Result.success("密码修改成功，请重新登录");
        } else {
            return Result.error("密码修改失败");
        }
    }

    @GetMapping("/sendForgetCode")
    @Operation(summary = "发送忘记密码验证码", description = "向已注册邮箱发送验证码")
    public Result sendForgetCode(@RequestParam("email") String email) {
        User existUser = userService.findUserByEmail(email);
        if (existUser == null) {
            return Result.error("该邮箱未注册");
        }

        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        String redisKey = "forget:code:" + email;
        stringRedisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
        mailUtil.sendVerificationCode(email, code);

        return Result.success("验证码已发送，5分钟内有效", null);
    }

    @PostMapping("/forgotPassword")
    @Operation(summary = "忘记密码", description = "通过邮箱验证码重置密码")
    public Result forgotPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");

        if (email == null || code == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        if (newPassword.length() < 6) {
            return Result.error("密码长度至少为6位");
        }

        String redisKey = "forget:code:" + email;
        String savedCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (savedCode == null || !savedCode.equals(code)) {
            return Result.error("验证码错误或已过期");
        }

        User existUser = userService.findUserByEmail(email);
        if (existUser == null) {
            return Result.error("用户不存在");
        }

        existUser.setPassword(PasswordUtil.encode(newPassword));
        userService.updateById(existUser);

        stringRedisTemplate.delete(redisKey);
        return Result.success("密码重置成功，请使用新密码登录");
    }

}
