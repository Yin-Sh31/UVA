package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息实体类
 * 用于农户和飞手之间的对话功能
 */
@Data
@TableName("message")
public class Message {
    
    /**
     * 消息ID（主键）
     */
    @TableId(type = IdType.AUTO, value = "message_id")
    private Long messageId;
    
    /**
     * 会话ID（用于标识唯一的对话）
     */
    @TableField("conversation_id")
    private String conversationId;
    
    /**
     * 发送者ID（关联sys_user.user_id）
     */
    @TableField("sender_id")
    private Long senderId;
    
    /**
     * 接收者ID（关联sys_user.user_id）
     */
    @TableField("receiver_id")
    private Long receiverId;
    
    /**
     * 消息类型：1-文本消息 2-图片 3-订单相关
     */
    @TableField("message_type")
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    @TableField("is_read")
    private Integer isRead;
    
    /**
     * 关联订单ID
     */
    @TableField("related_order_id")
    private Long relatedOrderId;
    
    /**
     * 发送时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    /**
     * 发送者名称（非数据库字段，用于返回给前端）
     */
    @TableField(exist = false)
    private String senderName;
    
    /**
     * 接收者名称（非数据库字段，用于返回给前端）
     */
    @TableField(exist = false)
    private String receiverName;

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

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
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

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}