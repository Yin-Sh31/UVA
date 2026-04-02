package com.cty.nopersonfinally.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@ApiModel("飞手信息更新DTO")
public class FlyerUpdateDTO {
    @ApiModelProperty("位置（经纬度，格式：lat,lon）")
    @Pattern(regexp = "^\\d+\\.\\d+,\\d+\\.\\d+$", message = "位置格式错误（示例：39.9087,116.3975）")
    private String location;
    
    @ApiModelProperty("每亩价格（元）")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private Double pricePerAcre;

    @ApiModelProperty("是否空闲：0-忙碌、1-空闲")
    private Integer isFree;

    @ApiModelProperty("技能等级：喷洒认证、巡检认证、全能认证")
    private String skillLevel;
    
    @ApiModelProperty("个人简介")
    private String introduction;
    
    @ApiModelProperty("手机号")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
    
    @ApiModelProperty("头像URL")
    private String avatar;


    public @Pattern(regexp = "^\\d+\\.\\d+,\\d+\\.\\d+$", message = "位置格式错误（示例：39.9087,116.3975）") String getLocation() {
        return location;
    }

    public void setLocation(@Pattern(regexp = "^\\d+\\.\\d+,\\d+\\.\\d+$", message = "位置格式错误（示例：39.9087,116.3975）") String location) {
        this.location = location;
    }

    public @DecimalMin(value = "0.01", message = "价格必须大于0") Double getPricePerAcre() {
        return pricePerAcre;
    }

    public void setPricePerAcre(@DecimalMin(value = "0.01", message = "价格必须大于0") Double pricePerAcre) {
        this.pricePerAcre = pricePerAcre;
    }

    public Integer getIsFree() {
        return isFree;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setIsFree(Integer isFree) {
        this.isFree = isFree;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误") String getPhone() {
        return phone;
    }

    public void setPhone(@Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误") String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}