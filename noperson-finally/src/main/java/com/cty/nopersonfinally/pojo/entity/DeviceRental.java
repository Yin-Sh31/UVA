package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 设备租借记录实体类
 */

@TableName("device_rental")
public class DeviceRental {
    
    @TableId(type = IdType.AUTO)
    private Long rentalId; // 租借记录ID（主键）
    
    private Long deviceId; // 设备ID（关联drone_device.device_id）
    
    private Long ownerId; // 机主ID（关联user_owner.id）
    
    private Long flyerId; // 飞手ID（关联sys_user.user_id）
    
    private Date rentalStartTime; // 租借开始时间
    
    private Date rentalEndTime; // 租借结束时间
    
    private Integer rentalStatus; // 租借状态：1-租借中 2-已归还 3-已取消
    
    private BigDecimal rentalAmount; // 租借费用
    
    private Integer paymentStatus; // 支付状态：0-未支付 1-已支付 2-支付失败
    
    private Date paymentTime; // 支付时间
    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime; // 创建时间
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime; // 更新时间
    
    private String remark; // 备注


    public Long getRentalId() {
        return rentalId;
    }

    public void setRentalId(Long rentalId) {
        this.rentalId = rentalId;
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

    public Date getRentalStartTime() {
        return rentalStartTime;
    }

    public void setRentalStartTime(Date rentalStartTime) {
        this.rentalStartTime = rentalStartTime;
    }

    public Date getRentalEndTime() {
        return rentalEndTime;
    }

    public void setRentalEndTime(Date rentalEndTime) {
        this.rentalEndTime = rentalEndTime;
    }

    public Integer getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(Integer rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public BigDecimal getRentalAmount() {
        return rentalAmount;
    }

    public void setRentalAmount(BigDecimal rentalAmount) {
        this.rentalAmount = rentalAmount;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Date getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime) {
        this.paymentTime = paymentTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}