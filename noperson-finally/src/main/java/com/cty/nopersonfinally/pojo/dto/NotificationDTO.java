package com.cty.nopersonfinally.pojo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通知消息DTO，用于前端展示
 */
@Data
public class NotificationDTO {
    private Long noticeId; // 消息ID
    private String title; // 消息标题
    private String content; // 消息内容
    private String type; // 消息类型（可读形式，如：系统通知、订单通知等）
    
    // 将类型代码转换为可读形式
    public void setTypeWithCode(String typeCode) {
        if (typeCode != null) {
            switch (typeCode) {
                case "2":
                    this.type = "订单通知";
                    break;
                case "3":
                    this.type = "审核通知";
                    break;
                case "4":
                    this.type = "评价通知";
                    break;
                default:
                    this.type = "系统通知";
            }
        } else {
            this.type = "系统通知";
        }
    }
    private Integer isRead; // 阅读状态（0-未读，1-已读）
    private String createTime; // 创建时间（格式化后的字符串）
    private Long relatedId; // 关联的订单/需求ID
    private String relatedType; // 关联类型（如SPRAY-喷洒订单，INSPECTION-巡检订单）
    
    // 时间格式化方法
    public void setFormattedTime(LocalDateTime time) {
        if (time != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.createTime = time.format(formatter);
        }
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }
}