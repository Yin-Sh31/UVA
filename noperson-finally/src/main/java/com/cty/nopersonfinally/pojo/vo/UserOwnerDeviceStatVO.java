package com.cty.nopersonfinally.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "机主设备统计VO")
public class UserOwnerDeviceStatVO {

    @Schema(description = "机主ID")
    private Long ownerId;

    @Schema(description = "机主姓名")
    private String ownerName;

    @Schema(description = "设备总数")
    private Integer total;

    @Schema(description = "可用设备数")
    private Integer available;

    @Schema(description = "工作中设备数")
    private Integer working;

    @Schema(description = "维护中设备数")
    private Integer maintaining;

    @Schema(description = "设备类型分布（key：类型名称，value：数量）")
    private Map<String, Integer> typeDistribution;


    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public Integer getWorking() {
        return working;
    }

    public void setWorking(Integer working) {
        this.working = working;
    }

    public Integer getMaintaining() {
        return maintaining;
    }

    public void setMaintaining(Integer maintaining) {
        this.maintaining = maintaining;
    }

    public Map<String, Integer> getTypeDistribution() {
        return typeDistribution;
    }

    public void setTypeDistribution(Map<String, Integer> typeDistribution) {
        this.typeDistribution = typeDistribution;
    }
}