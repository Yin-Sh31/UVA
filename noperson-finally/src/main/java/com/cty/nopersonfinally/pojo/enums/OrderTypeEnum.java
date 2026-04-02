package com.cty.nopersonfinally.pojo.enums;

import lombok.Getter;

@Getter
public enum OrderTypeEnum {
    SPRAY(1, "喷洒"),
    INSPECTION(2, "巡检");
    
    private final Integer code;
    private final String desc;
    
    OrderTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public static String getDescByCode(Integer code) {
        for (OrderTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type.getDesc();
            }
        }
        return "未知类型";
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}