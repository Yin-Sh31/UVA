package com.cty.nopersonfinally.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter

public enum SprayDemandStatusEnum {
    PENDING(0, "待接取"),
    PROCESSING(1, "处理中"),
    IN_PROGRESS(2, "作业中"),
    WAITING_CONFIRM(3, "待确认"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String desc;

    // 根据code获取描述
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知状态";
        }
        for (SprayDemandStatusEnum status : values()) {
            if (status.code == code) {
                return status.desc;
            }
        }
        return "未知状态";
    }

    // 根据code获取枚举
    public static SprayDemandStatusEnum getByCode(int code) {
        for (SprayDemandStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    SprayDemandStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}