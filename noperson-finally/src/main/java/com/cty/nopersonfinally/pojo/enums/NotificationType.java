package com.cty.nopersonfinally.pojo.enums;

import lombok.Data;


public enum NotificationType {
    MINIPROGRAM("MINIPROGRAM"), // 小程序通知
    SYSTEM("SYSTEM"); // 站内信通知

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

}