package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "设备维护记录创建DTO")
public class DeviceMaintainDTO {

    @Schema(description = "设备ID", example = "1001")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(description = "维护类型（1：常规保养 2：故障维修）", example = "1")
    @NotNull(message = "维护类型不能为空")
    private Integer maintainType;

    @Schema(description = "故障描述（维修时必填）", example = "电池续航下降")
    private String faultDescription;

    @Schema(description = "维护内容", example = "更换电池，系统升级")
    @NotBlank(message = "维护内容不能为空")
    @Size(max = 500, message = "维护内容不能超过500字符")
    private String maintainContent;

    @Schema(description = "维护费用（元）", example = "300.00")
    @DecimalMin(value = "0.01", message = "维护费用必须大于0")
    private Double cost;

    @Schema(description = "更换配件（逗号分隔）", example = "电池,螺旋桨")
    private String replaceParts;


    public @NotNull(message = "设备ID不能为空") Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(@NotNull(message = "设备ID不能为空") Long deviceId) {
        this.deviceId = deviceId;
    }

    public @NotNull(message = "维护类型不能为空") Integer getMaintainType() {
        return maintainType;
    }

    public void setMaintainType(@NotNull(message = "维护类型不能为空") Integer maintainType) {
        this.maintainType = maintainType;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public @NotBlank(message = "维护内容不能为空") @Size(max = 500, message = "维护内容不能超过500字符") String getMaintainContent() {
        return maintainContent;
    }

    public void setMaintainContent(@NotBlank(message = "维护内容不能为空") @Size(max = 500, message = "维护内容不能超过500字符") String maintainContent) {
        this.maintainContent = maintainContent;
    }

    public @DecimalMin(value = "0.01", message = "维护费用必须大于0") Double getCost() {
        return cost;
    }

    public void setCost(@DecimalMin(value = "0.01", message = "维护费用必须大于0") Double cost) {
        this.cost = cost;
    }

    public String getReplaceParts() {
        return replaceParts;
    }

    public void setReplaceParts(String replaceParts) {
        this.replaceParts = replaceParts;
    }
}