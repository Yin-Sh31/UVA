package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 */
@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    @TableField("notification_id")
    private Long noticeId;

    @TableField("user_id")
    private Long userId; // 接收用户ID

    private String title; // 通知标题

    private String content; // 通知内容

    @TableField("notify_type")
    private String type; // 通知类型（MINIPROGRAM-小程序，SYSTEM-站内信）

    @TableField("is_read")
    private Integer isRead; // 阅读状态（0-未读，1-已读）

    @TableField("send_time")
    private LocalDateTime createTime;


    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}