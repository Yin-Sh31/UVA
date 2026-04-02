package com.cty.nopersonfinally.pojo.dto;

import lombok.Data;

/**
 * 消息数据传输对象
 * 用于接收前端发送消息的请求数据
 */
@Data
public class MessageDTO {
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息类型：1-文本消息 2-图片 3-订单相关
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 关联订单ID（可选）
     */
    private Long relatedOrderId;


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

    public Long getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Long relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }
}