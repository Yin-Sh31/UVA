package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 交易记录实体类
 * 记录用户的收入和支出明细
 */
@Data
@TableName("transaction_record")
public class TransactionRecord {

    public Long getRelatedDemandId() {
        return relatedDemandId;
    }

    public void setRelatedDemandId(Long relatedDemandId) {
        this.relatedDemandId = relatedDemandId;
    }

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID（关联sys_user表）
     */
    private Long userId;
    
    /**
     * 交易金额
     * 正数表示收入，负数表示支出
     */
    private Double amount;
    
    /**
     * 交易类型
     * INCOME: 收入
     * EXPENSE: 支出
     */
    private String transactionType;
    
    /**
     * 交易状态
     * 1: 成功
     * 0: 失败
     */
    private Integer status;
    
    /**
     * 交易描述
     */
    private String description;
    
    /**
     * 关联业务ID（已使用relatedDemandId字段替代）
     *//* 数据库中不存在此列，已注释掉 */
    
    /**
     * 交易时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 关联需求ID
     */
    @TableField(value = "related_demand_id")
    private Long relatedDemandId;

//    /**
//     * 创建人
//     */
//    private Long createBy;
//
//    /**
//     * 更新人
//     */
//    private Long updateBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /* 数据库中不存在此列，已注释掉 */
    /*
    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }
    */

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

//    public Long getCreateBy() {
//        return createBy;
//    }
//
//    public void setCreateBy(Long createBy) {
//        this.createBy = createBy;
//    }
//
//    public Long getUpdateBy() {
//        return updateBy;
//    }
//
//    public void setUpdateBy(Long updateBy) {
//        this.updateBy = updateBy;
//    }
}