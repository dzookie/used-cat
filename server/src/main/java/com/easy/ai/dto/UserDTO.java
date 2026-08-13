package com.easy.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotNull
    @Email(message = "邮箱格式错误")
    private String email;

    @NotNull
    @Size(max = 20, min = 6, message = "密码长度应为6~20位")
    private String password;
}
