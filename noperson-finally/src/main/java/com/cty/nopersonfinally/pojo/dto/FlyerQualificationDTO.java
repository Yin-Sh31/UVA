package com.cty.nopersonfinally.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@ApiModel("飞手资质DTO")
public class FlyerQualificationDTO {
    @ApiModelProperty("执照类型")
    @NotBlank(message = "执照类型不能为空")
    private String licenseType;

    @ApiModelProperty("执照编号")
    @NotBlank(message = "执照编号不能为空")
    private String licenseNo;

    @ApiModelProperty("保险编号（可选）")
    private String insuranceNo;


    public @NotBlank(message = "执照类型不能为空") String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(@NotBlank(message = "执照类型不能为空") String licenseType) {
        this.licenseType = licenseType;
    }

    public @NotBlank(message = "执照编号不能为空") String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(@NotBlank(message = "执照编号不能为空") String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public String getInsuranceNo() {
        return insuranceNo;
    }

    public void setInsuranceNo(String insuranceNo) {
        this.insuranceNo = insuranceNo;
    }
}