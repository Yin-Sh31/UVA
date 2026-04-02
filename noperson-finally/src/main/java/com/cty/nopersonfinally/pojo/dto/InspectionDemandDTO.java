package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InspectionDemandDTO {
    private Long farmerId;

    @NotBlank(message = "地块名称不能为空")
    @Schema(description = "地块名称", example = "东河农场1号田")
    private String landName;

    @NotBlank(message = "地块边界不能为空")
    @Pattern(regexp = "^([\\d.]+,[\\d.]+)(;([\\d.]+,[\\d.]+))*$", message = "地块边界格式错误（示例：lat1,lng1 或 lat1,lng1;lat2,lng2）")
    private String landBoundary;

    @NotNull(message = "巡检目的不能为空")
    @Min(value = 1, message = "巡检目的只能是1-3")
    @Max(value = 3, message = "巡检目的只能是1-3")
    private Integer inspectionPurpose;

    @NotBlank(message = "期望分辨率不能为空")
    private String expectedResolution;

    @NotNull(message = "期望时间不能为空")
    private LocalDate expectedTime;

    @NotNull(message = "预算不能为空")
    @DecimalMin(value = "0.01", message = "预算必须大于0")
    private Double budget;

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public @NotBlank(message = "地块名称不能为空") String getLandName() {
        return landName;
    }

    public void setLandName(@NotBlank(message = "地块名称不能为空") String landName) {
        this.landName = landName;
    }

    public @NotBlank(message = "地块边界不能为空") @Pattern(regexp = "^([\\d.]+,[\\d.]+;)+([\\d.]+,[\\d.]+)$", message = "地块边界格式错误（示例：lat1,lng1;lat2,lng2）") String getLandBoundary() {
        return landBoundary;
    }

    public void setLandBoundary(@NotBlank(message = "地块边界不能为空") @Pattern(regexp = "^([\\d.]+,[\\d.]+;)+([\\d.]+,[\\d.]+)$", message = "地块边界格式错误（示例：lat1,lng1;lat2,lng2）") String landBoundary) {
        this.landBoundary = landBoundary;
    }

    public @NotNull(message = "巡检目的不能为空") @Min(value = 1, message = "巡检目的只能是1-3") @Max(value = 3, message = "巡检目的只能是1-3") Integer getInspectionPurpose() {
        return inspectionPurpose;
    }

    public void setInspectionPurpose(@NotNull(message = "巡检目的不能为空") @Min(value = 1, message = "巡检目的只能是1-3") @Max(value = 3, message = "巡检目的只能是1-3") Integer inspectionPurpose) {
        this.inspectionPurpose = inspectionPurpose;
    }

    public @NotBlank(message = "期望分辨率不能为空") String getExpectedResolution() {
        return expectedResolution;
    }

    public void setExpectedResolution(@NotBlank(message = "期望分辨率不能为空") String expectedResolution) {
        this.expectedResolution = expectedResolution;
    }

    public @NotNull(message = "期望时间不能为空") LocalDate getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(@NotNull(message = "期望时间不能为空") LocalDate expectedTime) {
        this.expectedTime = expectedTime;
    }

    public @NotNull(message = "预算不能为空") @DecimalMin(value = "0.01", message = "预算必须大于0") Double getBudget() {
        return budget;
    }

    public void setBudget(@NotNull(message = "预算不能为空") @DecimalMin(value = "0.01", message = "预算必须大于0") Double budget) {
        this.budget = budget;
    }
}