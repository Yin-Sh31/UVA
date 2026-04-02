package com.cty.nopersonfinally.pojo.enums;



/**
 * 响应状态码枚举
 * 统一管理系统中常用的响应状态码和对应消息
 */

public enum ResultCode {

    // 通用状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_BUSY(503, "系统繁忙，请稍后再试"),

    // 用户相关状态码（1000-1999）
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_AUDIT_PENDING(1003, "用户未通过审核，暂不能操作"),
    USER_HAS_BEEN_BANNED(1004, "用户已被封禁"),

    // 订单相关状态码（2000-2999）
    ORDER_NOT_EXIST(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(2003, "当前订单不能取消"),

    // 设备相关状态码（3000-3999）
    DEVICE_NOT_EXIST(3001, "设备不存在"),
    DEVICE_NOT_IDLE(3002, "设备不处于空闲状态"),
    DEVICE_AUDIT_PENDING(3003, "设备未通过审核"),

    // 农业服务相关状态码（4000-4999）

    INSPECTION_REPORT_NOT_EXIST(4001, "巡检报告不存在"),
    SPRAY_NEED_INSPECTION(4002, "喷洒需求必须关联巡检报告"),
    LAND_BOUNDARY_ERROR(4003, "地块边界格式错误"), 
    INVALID_PARAMS(4004, "参数错误"), 
    INTERNAL_SERVER_ERROR(5000, "服务器内部错误");

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
