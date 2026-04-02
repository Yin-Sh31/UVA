package com.cty.nopersonfinally.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 飞手信息展示VO
 */
@Data
@Schema(description = "飞手个人信息展示")
public class UserFlyerVO {
    @Schema(description = "用户名")
    private String userName;
    
    @Schema(description = "简介")
    private String introduction;

    @Schema(description = "执照类型（如：AOPA证书、CAAC证书）")
    private String licenseType;

    @Schema(description = "执照编号")
    private String licenseNo;

    @Schema(description = "执照文件URL（OSS存储路径）")
    private String licenseUrl;

    @Schema(description = "保险编号")
    private String insuranceNo;

    @Schema(description = "保险文件URL（OSS存储路径）")
    private String insuranceUrl;

    @Schema(description = "技能等级：喷洒认证、巡检认证、全能认证")
    private String skillLevel;

    @Schema(description = "经验")
    private String experience;

    @Schema(description = "所在位置（经纬度，格式：lat,lon）")
    private String location;

    @Schema(description = "信誉分（0-100，初始50）")
    private Double reputation;

    @Schema(description = "每亩作业价格（单位：元）")
    private Double pricePerAcre;

    @Schema(description = "是否空闲：0-忙碌、1-空闲")
    private Integer isFree;

    @Schema(description = "审核状态：0-待审核、1-通过、2-拒绝")
    private Integer auditStatus;

    @Schema(description = "审核结果")
    private String auditResult;

    @Schema(description = "审核不通过原因")
    private String auditRemark;

    @Schema(description = "资质")
    private String qualifications;

    @Schema(description = "信用分（初始0）")
    private Integer creditScore;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "登录用户名")
    private String username;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "角色类型")
    private Integer roleType;
    
    @Schema(description = "用户状态")
    private Integer status;
    
    @Schema(description = "余额")
    private Double balance;
    
    @Schema(description = "已完成订单数量")
    private Integer completedOrders;
    
    @Schema(description = "综合评分（0-5分）")
    private Double totalScore;
    
    @Schema(description = "星级（1-5星）")
    private Integer starLevel;
    
    @Schema(description = "评价总次数")
    private Integer evaluationCount;
    
    @Schema(description = "好评率（%）")
    private Double positiveRate;

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
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

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
    
    public Integer getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
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
}