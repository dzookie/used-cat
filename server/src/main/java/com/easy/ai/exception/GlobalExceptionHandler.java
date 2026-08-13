package com.easy.ai.exception;

import com.easy.ai.common.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(Exception.class)
//    public Result handleException(Exception e) {
//        e.printStackTrace();
//        return Result.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败");
//    }

    /**
     * 400 - 参数校验失败异常
     * 处理 @RequestBody @Valid 校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "参数错误",
                        (existing, replacement) -> existing + "; " + replacement
                ));
        StringBuilder errorBuilder = new StringBuilder();
        errors.forEach((key, value) -> errorBuilder.append(value).append("; "));
        String errorMsg = errorBuilder.toString().trim();
        return Result.error(errorMsg);
    }

    /**
     * 400 - 参数校验失败异常
     * 处理 @RequestParam/@PathVariable 校验失败（@Validated 在方法参数上）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> {
                            // 提取字段名：从 propertyPath 中获取
                            String path = violation.getPropertyPath().toString();
                            if (path.contains(".")) {
                                // 格式：methodName.argName.propertyName
                                String[] parts = path.split("\\.");
                                return parts.length > 1 ? parts[parts.length - 1] : path;
                            }
                            return path;
                        },
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));
        StringBuilder errorBuilder = new StringBuilder();
        errors.forEach((key, value) -> errorBuilder.append(value).append("; "));
        String errorMsg = errorBuilder.toString().trim();
        return Result.error(errorMsg);
    }

    /**
     * 400 - Spring Boot 3.x 新的参数校验异常
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();
            String errorMsg = result.getResolvableErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage() != null
                            ? error.getDefaultMessage()
                            : "参数错误")
                    .collect(Collectors.joining("; "));
            errors.put(paramName, errorMsg);
        });
        StringBuilder errorBuilder = new StringBuilder();
        errors.forEach((key, value) -> errorBuilder.append(value).append("; "));
        String errorMsg = errorBuilder.toString().trim();
        return Result.error(errorMsg);
    }

    /**
     * 500 - 服务器内部错误
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception ex) {
        System.out.println(ex);
        // 生产环境可以隐藏详细错误信息
        String errorMsg = "服务器内部错误";

        // 开发环境显示详细错误
        if ("dev".equals(System.getenv("SPRING_PROFILES_ACTIVE"))) {
            errorMsg = ex.getMessage();
        }

        return Result.error(errorMsg);
    }
}