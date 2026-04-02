package com.cty.nopersonfinally.pojo.enums;

public enum DemandStatusEnum {
    PENDING("PENDING", "待匹配"),
    MATCHED("MATCHED", "已匹配"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    DemandStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 构造器、getter省略


    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}