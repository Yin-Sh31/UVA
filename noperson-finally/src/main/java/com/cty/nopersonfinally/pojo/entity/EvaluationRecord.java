package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
// 评价记录
@Data
@TableName("evaluation_record")
public class EvaluationRecord {
    @TableId(type = IdType.AUTO)
    private Long evaluationId;

    private String orderType; // 订单类型
    private Long orderId; // 订单ID
    private Long evaluatorId; // 评价人ID
    private Long evaluateeId; // 被评价人ID
    private Integer score; // 评分（1-5星）
    private String content; // 评价内容
    private Integer evaluateeRole; // 被评价人角色（1-飞手 2-农户）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(Long evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public Long getEvaluateeId() {
        return evaluateeId;
    }

    public void setEvaluateeId(Long evaluateeId) {
        this.evaluateeId = evaluateeId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getEvaluateeRole() {
        return evaluateeRole;
    }

    public void setEvaluateeRole(Integer evaluateeRole) {
        this.evaluateeRole = evaluateeRole;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}