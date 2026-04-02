package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 设备操作日志实体类
 */
@Data
@TableName("t_device_operate_log")
public class DeviceOperateLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 操作员ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作前状态
     */
    @TableField("before_status")
    private Integer beforeStatus;

    /**
     * 操作后状态
     */
    @TableField("after_status")
    private Integer afterStatus;

    /**
     * 操作类型（1：状态变更 2：绑定飞手 3：解绑飞手 4：维护记录）
     */
    @TableField("operate_type")
    private Integer operateType;

    /**
     * 操作备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private Date operateTime;

    /**
     * 操作IP
     */
    @TableField("operate_ip")
    private String operateIp;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getOperateType() {
        return operateType;
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperateIp() {
        return operateIp;
    }

    public void setOperateIp(String operateIp) {
        this.operateIp = operateIp;
    }
}
