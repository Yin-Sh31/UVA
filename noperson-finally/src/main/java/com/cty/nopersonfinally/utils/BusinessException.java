package com.cty.nopersonfinally.utils;

import com.cty.nopersonfinally.pojo.enums.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常类
 * 用于封装业务逻辑中出现的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误状态码
     */
    private int code;

    /**
     * 构造方法：使用自定义消息
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500; // 默认使用500错误码
    }

    /**
     * 构造方法：使用预定义的错误码和消息
     * @param resultCode 预定义的错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    /**
     * 构造方法：使用自定义错误码和消息
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
