package com.cty.nopersonfinally.pojo.dto;

import com.cty.nopersonfinally.pojo.enums.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
 // 保留无参构造函数（Jackson必须）
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // 全参构造函数（可选，方便手动创建实例）
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Result() {
    }

    // 成功返回（带数据）
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功返回（无数据）
    public static Result<?> ok(String msg) {
        return new Result<>(200, msg, null);
    }

    // 错误返回
    public static Result<?> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // 错误返回（带状态码，修复问题）
    public static Result<?> error(ResultCode code) {
        // 假设 ResultCode 有 getCode() 和 getMessage() 方法
        return new Result<>(code.getCode(), code.getMsg(), null);
    }
    // 错误返回（带状态码和消息）
    public static Result<?> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}