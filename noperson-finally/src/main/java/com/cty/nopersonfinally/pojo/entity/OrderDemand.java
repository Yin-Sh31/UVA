package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;
//需求
@Data
@TableName("demand")
public class OrderDemand {
    @TableId(type = IdType.AUTO)
    private Long demandId; // 需求ID
    
    // 订单类型：1-喷洒，2-巡检
    private Integer orderType;

    private Long farmerId; // 农户ID
    private Long flyerId; // 飞手ID（接单后赋值）
    private Long reportId; // 关联巡检报告ID
    private Long deviceId; // 关联设备ID
    private Long landId; // 地块ID（可选，如果有独立地块管理时使用）

    private String landName; // 地块名称
    private String landBoundary; // 地块边界（经纬度）
    private String cropType; // 作物类型
    private String pestType; // 病虫害类型
    private String inspectionPurpose; // 巡检目的（仅巡检订单有效）
    private String expectedResolution; // 期望分辨率（cm，仅巡检订单有效）
    private String landLocation; // 地块位置
    private Double landArea; // 地块面积（亩）
    private LocalDateTime expectedTime; // 期望作业时间
    private Double budget; // 预算金额

    private Integer status; // 状态（0-待接取，1-处理中，2-作业中，3-待农户确认，4-待支付，5-已完成，6-已取消）

    // 时间字段
    private LocalDateTime acceptTime; // 接单时间
    private LocalDateTime startTime; // 开始作业时间
    private LocalDateTime completeTime; // 完成作业时间
    private LocalDateTime cancelTime; // 取消时间
    private String cancelReason; // 取消原因

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 支付相关字段
    private Double paymentAmount; // 实际支付金额
    private Integer paymentStatus; // 支付状态：0-未支付，1-已支付，2-支付失败
    private LocalDateTime paymentTime; // 支付时间
    private String paymentMethod; // 支付方式：balance-余额支付
    private Long paymentRecordId; // 支付记录ID


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

    public Long getFlyerId() {
        return flyerId;
    }

    public void setFlyerId(Long flyerId) {
        this.flyerId = flyerId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getLandId() {
        return landId;
    }

    public void setLandId(Long landId) {
        this.landId = landId;
    }

    public String getLandName() {
        return landName;
    }

    public void setLandName(String landName) {
        this.landName = landName;
    }

    public String getLandBoundary() {
        return landBoundary;
    }

    public void setLandBoundary(String landBoundary) {
        this.landBoundary = landBoundary;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getPestType() {
        return pestType;
    }

    public void setPestType(String pestType) {
        this.pestType = pestType;
    }
    
    public String getInspectionPurpose() {
        return inspectionPurpose;
    }
    
    public void setInspectionPurpose(String inspectionPurpose) {
        this.inspectionPurpose = inspectionPurpose;
    }

    public String getExpectedResolution() {
        return expectedResolution;
    }

    public void setExpectedResolution(String expectedResolution) {
        this.expectedResolution = expectedResolution;
    }

    public String getLandLocation() {
        return landLocation;
    }

    public void setLandLocation(String landLocation) {
        this.landLocation = landLocation;
    }

    public Double getLandArea() {
        return landArea;
    }

    public void setLandArea(Double landArea) {
        this.landArea = landArea;
    }

    public LocalDateTime getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(LocalDateTime expectedTime) {
        this.expectedTime = expectedTime;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public LocalDateTime getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(LocalDateTime acceptTime) {
        this.acceptTime = acceptTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
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

    // 支付相关字段的getter和setter


    public Double getPaymentAmount() {
        return paymentAmount;
    }



    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getPaymentRecordId() {
        return paymentRecordId;
    }

    public void setPaymentRecordId(Long paymentRecordId) {
        this.paymentRecordId = paymentRecordId;
    }
}