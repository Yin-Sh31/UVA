package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

/**
 * 机主资质上传DTO
 */
@Data
@Schema(description = "机主资质上传DTO")
public class OwnerQualificationDTO {

    @Schema(description = "执照类型")
    private String licenseType;

    @Schema(description = "执照编号")
    private String licenseNumber;

    @Schema(description = "常用作业区域")
    private String commonArea;
}