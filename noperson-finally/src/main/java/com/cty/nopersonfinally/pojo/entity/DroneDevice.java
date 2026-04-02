package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

// 设备信息
// main/java/com/cty/nopersonfinally/pojo/entity/DroneDevice.java
@Data
@TableName("drone_device")
public class DroneDevice {
    @TableId(type = IdType.AUTO)
    private Long deviceId;  // 设备ID
    private Long ownerId; // 机主ID（关联sys_user）
    @TableField(value = "flyer_id")
    private Long flyerId; // 绑定的飞手ID
    private String model; // 设备型号
    private String deviceName; // 设备名称
    @TableField(value = "device_no")
    private String serialNumber; // 设备序列号（唯一）
    @TableField(value = "brand")
    private String deviceType; // 设备类型（品牌）
    private Integer status; // 状态：0-停用 1-正常 2-维修中 3-已报废
    private LocalDateTime purchaseTime; // 购买时间（统一为LocalDateTime）
    private LocalDateTime lastMaintainTime; // 上次维护时间
    @TableField(exist = false)
    private Integer isQualified; // 设备认证状态（0：未认证 1：已认证）
    @TableField(value = "insurance_expire_time")
    private Date expireTime; // 认证有效期
    @TableField(value = "max_load")
    private Double maxLoad; // 设备最大载重（kg）
    @TableField(value = "endurance")
    private String endurance; // 续航时间（分钟）
    @TableField(exist = false)
    private String description; // 设备描述
    private String manufacturer;// 设备制造商
    private String picture; // 设备图片
    @TableField(value = "rental_status")
    private Integer rentalStatus;// 设备租赁状态：0-可租借 1-已租借 2-审核中
    @TableLogic
    private Integer isDeleted; // 是否删除（0：未删除 1：已删除）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Integer getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(Integer rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getFlyerId() {
        return flyerId;
    }

    public void setFlyerId(Long flyerId) {
        this.flyerId = flyerId;
    }

    public String getModel() {
        return model;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(LocalDateTime purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public LocalDateTime getLastMaintainTime() {
        return lastMaintainTime;
    }

    public void setLastMaintainTime(LocalDateTime lastMaintainTime) {
        this.lastMaintainTime = lastMaintainTime;
    }

    public Integer getIsQualified() {
        return isQualified;
    }

    public void setIsQualified(Integer isQualified) {
        this.isQualified = isQualified;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Double getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(Double maxLoad) {
        this.maxLoad = maxLoad;
    }

    public String getEndurance() {
        return endurance;
    }

    public void setEndurance(String endurance) {
        this.endurance = endurance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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