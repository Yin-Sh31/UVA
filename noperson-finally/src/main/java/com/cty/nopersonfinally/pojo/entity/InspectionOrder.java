// main/java/com/cty/nopersonfinally/pojo/entity/InspectionOrder.java
package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
// 巡检订单
@Data
@TableName("inspection_order")
public class InspectionOrder {
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private Long farmerId; // 农户ID
    private Long flyerId; // 飞手ID（匹配后赋值）

    private String landName; // 地块名称
    private String landBoundary; // 地块边界
    @TableField(exist = false) // 标记该字段在数据库表中不存在
    private Integer inspectionPurpose; // 巡检目的
    private String expectedResolution; // 期望分辨率
    private LocalDate expectedTime; // 期望时间
    private Double budget; // 预算

    // 状态改为Integer，对应OrderStatusEnum的code
    private Integer status; // 状态（0-待匹配，1-处理中，2-作业中，3-待确认，4-已完成，5-已取消）

    private Long deviceId; // 关联设备ID
    private LocalDateTime acceptTime; // 接单时间
    private LocalDateTime startTime; // 开始作业时间
    private LocalDateTime completeTime; // 完成作业时间

    // 新增取消相关字段
    private LocalDateTime cancelTime; // 取消时间
    private String cancelReason; // 取消原因（可选）

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public Integer getInspectionPurpose() {
        return inspectionPurpose;
    }

    public void setInspectionPurpose(Integer inspectionPurpose) {
        this.inspectionPurpose = inspectionPurpose;
    }

    public String getExpectedResolution() {
        return expectedResolution;
    }

    public void setExpectedResolution(String expectedResolution) {
        this.expectedResolution = expectedResolution;
    }

    public LocalDate getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(LocalDate expectedTime) {
        this.expectedTime = expectedTime;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
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
}