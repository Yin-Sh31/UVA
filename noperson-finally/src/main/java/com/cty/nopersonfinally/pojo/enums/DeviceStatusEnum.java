package com.cty.nopersonfinally.pojo.enums;

import lombok.Getter;

/**
 * 设备状态枚举
 */
@Getter
public enum DeviceStatusEnum {
    DISABLED(0, "停用"),
    NORMAL(1, "正常"),
    MAINTENANCE(2, "维修中"),
    SCRAPPED(3, "已报废");

    private final int code;
    private final String desc;

    DeviceStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举
     */
    public static DeviceStatusEnum getByCode(int code) {
        for (DeviceStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
