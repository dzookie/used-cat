package com.easy.ai.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <E> Result<E> success(String message, E data) {
        return new Result<>(200, message != null ? message:"操作成功", data);
    }

    public static Result success(String message){
        return new Result(200, message, null);
    }

    public static Result success(){
        return new Result(200, "操作成功", null);
    }

    public static Result error(String message){
        return new Result(500, message, null);
    }
}
