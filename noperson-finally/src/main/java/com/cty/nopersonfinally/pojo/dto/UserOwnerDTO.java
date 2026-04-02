package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "机主资料提交DTO")
public class UserOwnerDTO {

    @Schema(description = "运营执照类型（INDIVIDUAL-个人，ENTERPRISE-企业）", example = "INDIVIDUAL")
    @NotBlank(message = "执照类型不能为空")
    @Pattern(regexp = "^(INDIVIDUAL|ENTERPRISE)$", message = "执照类型只能是INDIVIDUAL或ENTERPRISE")
    private String licenseType;

    @Schema(description = "运营执照编号", example = "SY-202405001")
    @NotBlank(message = "执照编号不能为空")
    @Size(max = 50, message = "执照编号长度不能超过50字符")
    private String licenseNumber;

    @Schema(description = "执照照片URL列表（至少1张）", example = "[\"https://oss.example.com/license1.jpg\"]")
    @Size(min = 1, message = "至少上传1张执照照片")
    private List<String> licenseUrls;

    @Schema(description = "常用作业区域（省-市-县）", example = "山东省-青岛市-崂山区")
    @NotBlank(message = "常用作业区域不能为空")
    private String commonArea;


    public @NotBlank(message = "执照类型不能为空") @Pattern(regexp = "^(INDIVIDUAL|ENTERPRISE)$", message = "执照类型只能是INDIVIDUAL或ENTERPRISE") String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(@NotBlank(message = "执照类型不能为空") @Pattern(regexp = "^(INDIVIDUAL|ENTERPRISE)$", message = "执照类型只能是INDIVIDUAL或ENTERPRISE") String licenseType) {
        this.licenseType = licenseType;
    }

    public @NotBlank(message = "执照编号不能为空") @Size(max = 50, message = "执照编号长度不能超过50字符") String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(@NotBlank(message = "执照编号不能为空") @Size(max = 50, message = "执照编号长度不能超过50字符") String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public @Size(min = 1, message = "至少上传1张执照照片") List<String> getLicenseUrls() {
        return licenseUrls;
    }

    public void setLicenseUrls(@Size(min = 1, message = "至少上传1张执照照片") List<String> licenseUrls) {
        this.licenseUrls = licenseUrls;
    }

    public @NotBlank(message = "常用作业区域不能为空") String getCommonArea() {
        return commonArea;
    }

    public void setCommonArea(@NotBlank(message = "常用作业区域不能为空") String commonArea) {
        this.commonArea = commonArea;
    }
}