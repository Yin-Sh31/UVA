package com.cty.nopersonfinally.config;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.utils.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        log.warn("参数校验异常：{}", errorMsg);
        // 参数校验失败返回400状态码，而不是500
        return Result.error(400, "参数校验失败：" + String.join("；", errorMsg));
    }

    // 处理自定义业务异常
    @ExceptionHandler(BusinessException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST) // 业务异常返回400
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }


    // 处理其他未知异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleUnknownException(Exception e) {
        log.error("系统异常：", e);  // 记录完整堆栈
        System.err.println("系统异常详细信息：");
        e.printStackTrace();
        return Result.error(500, "系统繁忙，请稍后再试：" + e.getMessage());
    }
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Unsupported media type requested");
    }
}