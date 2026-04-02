package com.cty.nopersonfinally.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "机主详情VO（详情页展示用）")
public class UserOwnerDetailVO {

    @Schema(description = "机主ID")
    private Long id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "机主姓名")
    private String realName;

    @Schema(description = "联系电话（取自sys_user表）")
    private String phone;

    @Schema(description = "运营执照类型（INDIVIDUAL-个人，ENTERPRISE-企业）")
    private String licenseType;

    @Schema(description = "运营执照类型描述")
    private String licenseTypeDesc;

    @Schema(description = "执照编号（脱敏显示，如：SY-****001）")
    private String licenseNumberMasked;

    @Schema(description = "执照照片URL列表")
    private List<String> licenseUrls;

    @Schema(description = "管理设备总数")
    private Integer deviceTotal;

    @Schema(description = "可用设备数")
    private Integer availableDeviceCount;

    @Schema(description = "信誉分")
    private Integer creditScore;

    @Schema(description = "常用作业区域")
    private String commonArea;

    @Schema(description = "审核状态（0-待审核 1-已通过 2-已拒绝）")
    private Integer auditStatus;

    @Schema(description = "审核状态描述")
    private String auditStatusDesc;

    @Schema(description = "审核人（管理员姓名）")
    private String auditorName;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime auditTime;

    @Schema(description = "审核拒绝原因")
    private String rejectReason;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "最近更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    @Schema(description = "头像")
    private String avatar;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getLicenseTypeDesc() {
        return licenseTypeDesc;
    }

    public void setLicenseTypeDesc(String licenseTypeDesc) {
        this.licenseTypeDesc = licenseTypeDesc;
    }

    public String getLicenseNumberMasked() {
        return licenseNumberMasked;
    }

    public void setLicenseNumberMasked(String licenseNumberMasked) {
        this.licenseNumberMasked = licenseNumberMasked;
    }

    public List<String> getLicenseUrls() {
        return licenseUrls;
    }

    public void setLicenseUrls(List<String> licenseUrls) {
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

    public String getAuditStatusDesc() {
        return auditStatusDesc;
    }

    public void setAuditStatusDesc(String auditStatusDesc) {
        this.auditStatusDesc = auditStatusDesc;
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
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
    
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}