package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 需求反馈实体类
 */
@TableName("demand_feedback")
public class DemandFeedback {
    
    @TableId(type = IdType.AUTO)
    private Long id; // 反馈ID
    
    private Long demandId; // 需求ID
    
    private Long farmerId; // 农户ID
    
    private String farmerName; // 农户姓名
    
    private Long flyerId; // 飞手ID
    
    private String flyerName; // 飞手姓名
    
    private String feedbackType; // 反馈类型，如incomplete表示需求未完成
    
    private String feedbackContent; // 反馈内容
    
    private String feedbackImages; // 反馈图片URLs，多个用逗号分隔
    
    private String status; // 审核状态：pending-待审核，approved-已通过，rejected-已拒绝
    
    private Long adminId; // 审核管理员ID
    
    private String adminName; // 审核管理员姓名
    
    private LocalDateTime auditTime; // 审核时间
    
    private String auditRemark; // 审核备注
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public Long getFlyerId() {
        return flyerId;
    }

    public void setFlyerId(Long flyerId) {
        this.flyerId = flyerId;
    }

    public String getFlyerName() {
        return flyerName;
    }

    public void setFlyerName(String flyerName) {
        this.flyerName = flyerName;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getFeedbackContent() {
        return feedbackContent;
    }

    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }

    public String getFeedbackImages() {
        return feedbackImages;
    }

    public void setFeedbackImages(String feedbackImages) {
        this.feedbackImages = feedbackImages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
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
}