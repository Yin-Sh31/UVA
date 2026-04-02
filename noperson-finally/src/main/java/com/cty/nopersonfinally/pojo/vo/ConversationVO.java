package com.cty.nopersonfinally.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话视图对象
 * 用于返回给前端的会话列表数据
 */
@Data
public class ConversationVO {
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 对方用户ID
     */
    private Long otherUserId;
    
    /**
     * 对方用户名称
     */
    private String otherUserName;
    
    /**
     * 对方用户头像
     */
    private String otherUserAvatar;
    
    /**
     * 对方用户类型：0-农户 1-飞手
     */
    private Integer otherUserType;
    
    /**
     * 最后一条消息内容
     */
    private String lastMessageContent;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 未读消息数量
     */
    private Integer unreadCount;
    
    /**
     * 关联订单ID（如果有的话）
     */
    private Long relatedOrderId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserAvatar() {
        return otherUserAvatar;
    }

    public void setOtherUserAvatar(String otherUserAvatar) {
        this.otherUserAvatar = otherUserAvatar;
    }

    public Integer getOtherUserType() {
        return otherUserType;
    }

    public void setOtherUserType(Integer otherUserType) {
        this.otherUserType = otherUserType;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }
}