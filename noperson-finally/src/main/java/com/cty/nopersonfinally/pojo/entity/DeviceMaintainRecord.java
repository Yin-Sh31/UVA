package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

//设备保养记录
@Data
@TableName("device_maintain_record")
public class DeviceMaintainRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId;
    private Long deviceId; // 设备ID
    private Long operatorId; // 操作人ID（飞手/管理员）
    private Integer maintainType; // 维护类型（1：常规保养 2：故障维修）
    private String faultDescription; // 故障描述（维修时必填）
    private String maintainContent; // 维护内容
    private Double cost; // 维护费用
    private String replaceParts; // 更换配件（逗号分隔）
    private LocalDateTime maintainTime; // 维护时间
    private Integer status; // 状态（0：待审核 1：已确认）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Integer getMaintainType() {
        return maintainType;
    }

    public void setMaintainType(Integer maintainType) {
        this.maintainType = maintainType;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public String getMaintainContent() {
        return maintainContent;
    }

    public void setMaintainContent(String maintainContent) {
        this.maintainContent = maintainContent;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getReplaceParts() {
        return replaceParts;
    }

    public void setReplaceParts(String replaceParts) {
        this.replaceParts = replaceParts;
    }

    public LocalDateTime getMaintainTime() {
        return maintainTime;
    }

    public void setMaintainTime(LocalDateTime maintainTime) {
        this.maintainTime = maintainTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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