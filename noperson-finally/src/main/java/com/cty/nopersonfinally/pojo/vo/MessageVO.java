package com.cty.nopersonfinally.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息视图对象
 * 用于返回给前端的消息数据
 */
@Data
public class MessageVO {
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 会话ID
     */
    private String conversationId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者名称
     */
    private String senderName;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 接收者名称
     */
    private String receiverName;
    
    /**
     * 消息类型：1-文本消息 2-图片 3-订单相关
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;
    
    /**
     * 关联订单ID
     */
    private Long relatedOrderId;
    
    /**
     * 发送时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否是自己发送的消息（用于前端展示区分）
     */
    private Boolean FromMySelf;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Boolean getFromMySelf() {
        return FromMySelf;
    }

    public void setFromMySelf(Boolean fromMySelf) {
        FromMySelf = fromMySelf;
    }
}