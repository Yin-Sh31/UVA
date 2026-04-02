package com.cty.nopersonfinally.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "设备维护记录展示VO")
public class DeviceMaintainVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "维护类型（1：常规保养 2：故障维修）")
    private Integer maintainType;

    @Schema(description = "维护类型描述")
    private String maintainTypeDesc;

    @Schema(description = "故障描述")
    private String faultDescription;

    @Schema(description = "维护内容")
    private String maintainContent;

    @Schema(description = "维护费用（元）")
    private Double cost;

    @Schema(description = "维护时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime maintainTime;

    @Schema(description = "状态（0：待审核 1：已确认）")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;


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

    public Integer getMaintainType() {
        return maintainType;
    }

    public void setMaintainType(Integer maintainType) {
        this.maintainType = maintainType;
    }

    public String getMaintainTypeDesc() {
        return maintainTypeDesc;
    }

    public void setMaintainTypeDesc(String maintainTypeDesc) {
        this.maintainTypeDesc = maintainTypeDesc;
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

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }
}