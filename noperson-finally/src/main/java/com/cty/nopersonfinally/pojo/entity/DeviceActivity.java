package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备动态实体类
 */
@Data
@TableName("device_activity")
public class DeviceActivity {
    @TableId(type = IdType.AUTO)
    private Long activityId; // 动态ID
    private Long deviceId; // 设备ID
    private Integer activityType; // 动态类型：1-设备上线 2-设备下线 3-设备维护 4-设备归还 5-设备租借 6-状态变更
    private String activityDesc; // 动态描述
    private Long operatorId; // 操作人ID
    private String operatorName; // 操作人姓名
    private Integer beforeStatus; // 变更前状态
    private Integer afterStatus; // 变更后状态
    private LocalDateTime createTime; // 创建时间

    // 关联的设备信息（非数据库字段）
    @TableField(exist = false)
    private DroneDevice device; // 关联的设备信息

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getActivityType() {
        return activityType;
    }

    public void setActivityType(Integer activityType) {
        this.activityType = activityType;
    }

    public String getActivityDesc() {
        return activityDesc;
    }

    public void setActivityDesc(String activityDesc) {
        this.activityDesc = activityDesc;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Integer getBeforeStatus() {
        return beforeStatus;
    }

    public void setBeforeStatus(Integer beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    public Integer getAfterStatus() {
        return afterStatus;
    }

    public void setAfterStatus(Integer afterStatus) {
        this.afterStatus = afterStatus;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public DroneDevice getDevice() {
        return device;
    }

    public void setDevice(DroneDevice device) {
        this.device = device;
    }
}