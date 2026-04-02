package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 飞手信息实体
 */
@Data
@TableName("user_flyer")
public class UserFlyer {
    @TableId(type = IdType.AUTO)
    private Long flyerId; // 主键

    private Long userId; // 关联用户ID（sys_user表）

    private String userName; // 用户名（冗余，方便查询）

    private String introduction; //简介

    private String licenseType; // 执照类型（如：AOPA证书、CAAC证书）

    private String licenseNo; // 执照编号

    private String licenseUrl; // 执照文件URL（OSS存储路径）

    private String insuranceNo; // 保险编号

    private String insuranceUrl; // 保险文件URL（OSS存储路径）

    private String skillLevel; // 技能等级：喷洒认证、巡检认证、全能认证

    private String location; // 所在位置（经纬度，格式：lat,lon）

    private Double reputation; // 信誉分（0-100，初始50）

    private Double pricePerAcre; // 每亩作业价格（单位：元）

    private Integer isFree; // 是否空闲：0-忙碌、1-空闲

    private Integer auditStatus; // 审核状态：0-待审核、1-通过、2-拒绝

    private LocalDateTime auditTime; // 审核时间

    private String auditResult; // 审核结果（冗余，便于展示）

    private String auditRemark; // 审核不通过原因

    private LocalDateTime createTime; // 创建时间

    private  String  Qualifications; // 资质

    private String experience; // 经验(年)
    
    private Integer completedOrders; // 完成订单数量

    private Integer creditScore; // 信用分（初始0）
    
    /**
     * 综合评分（0-5分）
     */
    private Double totalScore;

    /**
     * 星级（1-5星）
     */
    private Integer starLevel;

    /**
     * 评价总次数
     */
    private Integer evaluationCount;

    /**
     * 好评率（%）
     */
    private Double positiveRate;

    /**
     * 被投诉次数
     */
    private Integer complaintCount;

    /**
     * 飞手状态（0-正常 1-受限 2-封禁）
     */
    private Integer creditStatus;

    private String avatar; // 头像URL

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime; // 更新时间（自动填充）

    @TableLogic
    private Integer isDeleted; // 逻辑删除：0-正常、1-删除

    public Long getFlyerId() {
        return flyerId;
    }

    public void setFlyerId(Long flyerId) {
        this.flyerId = flyerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getInsuranceNo() {
        return insuranceNo;
    }

    public void setInsuranceNo(String insuranceNo) {
        this.insuranceNo = insuranceNo;
    }

    public String getInsuranceUrl() {
        return insuranceUrl;
    }

    public void setInsuranceUrl(String insuranceUrl) {
        this.insuranceUrl = insuranceUrl;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getReputation() {
        return reputation;
    }

    public void setReputation(Double reputation) {
        this.reputation = reputation;
    }

    public Double getPricePerAcre() {
        return pricePerAcre;
    }

    public void setPricePerAcre(Double pricePerAcre) {
        this.pricePerAcre = pricePerAcre;
    }

    public Integer getIsFree() {
        return isFree;
    }

    public void setIsFree(Integer isFree) {
        this.isFree = isFree;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(String auditResult) {
        this.auditResult = auditResult;
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

    public String getQualifications() {
        return Qualifications;
    }

    public void setQualifications(String qualifications) {
        Qualifications = qualifications;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public Double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(Integer starLevel) {
        this.starLevel = starLevel;
    }

    public Integer getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(Integer evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public Double getPositiveRate() {
        return positiveRate;
    }

    public void setPositiveRate(Double positiveRate) {
        this.positiveRate = positiveRate;
    }

    public Integer getComplaintCount() {
        return complaintCount;
    }

    public void setComplaintCount(Integer complaintCount) {
        this.complaintCount = complaintCount;
    }

    public Integer getCreditStatus() {
        return creditStatus;
    }

    public void setCreditStatus(Integer creditStatus) {
        this.creditStatus = creditStatus;
    }
}