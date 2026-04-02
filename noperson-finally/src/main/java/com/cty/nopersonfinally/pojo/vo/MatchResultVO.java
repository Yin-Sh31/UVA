package com.cty.nopersonfinally.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



/**
 * 匹配结果VO
 */
@Data
@ApiModel("飞手-设备匹配结果")
public class MatchResultVO {
    @ApiModelProperty("飞手ID")
    private Long flyerId;

    @ApiModelProperty("飞手姓名")
    private String flyerName;

    @ApiModelProperty("设备ID")
    private Long deviceId;

    @ApiModelProperty("设备型号")
    private String deviceModel;

    @ApiModelProperty("匹配总得分")
    private Integer totalScore;

    @ApiModelProperty("预估价格")
    private Double estimatedPrice;


    public Long getFlyerId() {
        return flyerId;
    }

    public void setFlyerId(Long flyerId) {
        this.flyerId = flyerId;
    }

    public String getFlyerName() {
        return flyerName;
    }

    public void setFlyerName(String flyerName) {
        this.flyerName = flyerName;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(Double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }
}