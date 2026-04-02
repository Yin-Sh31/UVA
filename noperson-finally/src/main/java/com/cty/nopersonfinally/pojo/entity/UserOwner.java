package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机主信息实体（无人机设备拥有者）
 */
@Data
@TableName("user_owner")
public class UserOwner {
    @TableId(type = IdType.AUTO)
    private Long id; // 机主表主键

    @TableField
    private Long userId; // 关联用户ID，与登录账号绑定

    @TableField
    private String realName;

    @TableField
    private String licenseType;

    @TableField
    private String licenseNumber;

    @TableField
    private String licenseUrls;

    @TableField
    private Integer deviceTotal;

    @TableField
    private Integer availableDeviceCount;

    @TableField
    private Integer creditScore;

    @TableField
    private String commonArea;

    @TableField
    private Integer auditStatus;

    @TableField
    private Long auditorId;

    @TableField
    private LocalDateTime auditTime;

    @TableField
    private String rejectReason;

    @TableField
    private LocalDateTime createTime;

    @TableField
    private LocalDateTime updateTime;

    @TableLogic
    @TableField
    private Integer isDeleted;

    /**
     * 头像URL
     */
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseUrls() {
        return licenseUrls;
    }

    public void setLicenseUrls(String licenseUrls) {
        this.licenseUrls = licenseUrls;
    }

    public Integer getDeviceTotal() {
        return deviceTotal;
    }

    public void setDeviceTotal(Integer deviceTotal) {
        this.deviceTotal = deviceTotal;
    }

    public Integer getAvailableDeviceCount() {
        return availableDeviceCount;
    }

    public void setAvailableDeviceCount(Integer availableDeviceCount) {
        this.availableDeviceCount = availableDeviceCount;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public String getCommonArea() {
        return commonArea;
    }

    public void setCommonArea(String commonArea) {
        this.commonArea = commonArea;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}