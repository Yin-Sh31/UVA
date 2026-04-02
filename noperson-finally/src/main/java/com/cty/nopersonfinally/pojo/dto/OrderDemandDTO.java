package com.cty.nopersonfinally.pojo.dto;

import  io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Data   
@Schema(description = "需求创建DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDemandDTO {
    @Schema(description = "订单类型：1-喷洒，2-巡检")
    @NotNull(message = "订单类型不能为空")
    @Min(value = 1, message = "订单类型无效")
    @Max(value = 2, message = "订单类型无效")
    private Integer orderType;

    @Schema(description = "农户ID（后端自动填充，前端无需传递）")
    private Long farmerId;

    @Schema(description = "关联的巡检报告ID（已废弃，不再使用）", example = "1001")
    private Long reportId;
    
    @Schema(description = "地块ID（可选，有独立地块管理时使用）", example = "100")
    private Long landId;

    @Schema(description = "地块名称（必填）", example = "东河沿岸小麦田")
    @NotBlank(message = "地块名称不能为空")
    @Size(max = 100, message = "地块名称长度不能超过100字符")
    private String landName;

    @Schema(description = "地块边界（经纬度集合，格式：lat1,lng1;lat2,lng2;...，必填）",
            example = "39.9042,116.4074;39.9043,116.4075;39.9044,116.4076")
    @NotBlank(message = "地块边界不能为空")
    @Pattern(regexp = "^([\\d.]+,[-\\d.]+;)+([\\d.]+,[-\\d.]+)$",
            message = "地块边界格式错误，正确格式：lat1,lng1;lat2,lng2;...")
    private String landBoundary;

    @Schema(description = "作物类型（必填）", example = "小麦")
    @NotBlank(message = "作物类型不能为空")
    @Size(max = 50, message = "作物类型长度不能超过50字符")
    private String cropType;

    @Schema(description = "病虫害类型（喷洒订单必填，巡检订单可选）", example = "蚜虫")
    @Size(max = 100, message = "病虫害类型长度不能超过100字符")
    private String pestType;
    
    @Schema(description = "巡检目的（巡检订单必填，喷洒订单无需填写）", example = "病虫害检测")
    @Size(max = 200, message = "巡检目的长度不能超过200字符")
    private String inspectionPurpose;
    
    @Schema(description = "期望分辨率（巡检订单必填，单位cm，喷洒订单无需填写）", example = "5")
    @Min(value = 1, message = "期望分辨率必须大于0")
    @Max(value = 50, message = "期望分辨率不能超过50cm")
    private String expectedResolution;

    @Schema(description = "地块位置（行政区划，如“河北省石家庄市正定县”，必填）", example = "河北省石家庄市正定县")
    @NotBlank(message = "地块位置不能为空")
    @Size(max = 200, message = "地块位置长度不能超过200字符")
    private String landLocation;

    @Schema(description = "地块面积（亩，必填，需大于0）", example = "50.5")
    @NotNull(message = "地块面积不能为空")
    @Positive(message = "地块面积必须大于0")
    private Double landArea;

    @Schema(description = "期望作业时间（必填，需为未来时间）", example = "2024-10-15T09:30:00")
    @NotNull(message = "期望作业时间不能为空")
    @Future(message = "期望作业时间必须为未来时间")
    private LocalDateTime expectedTime;

    @Schema(description = "预算金额（元，必填，需大于0）", example = "3000.00")
    @NotNull(message = "预算金额不能为空")
    @Positive(message = "预算金额必须大于0")
    private Double budget;

    @Schema(description = "特殊要求（可选，如农药类型、作业禁忌等）", example = "需使用生物农药，避免高温作业")
    @Size(max = 500, message = "特殊要求长度不能超过500字符")
    private String specialRequirements;
    
    /**
     * 订单状态（前端可能会传递此字段，但后端不使用）
     * 添加此字段以避免Jackson序列化错误
     */
    private Integer status;

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public @NotBlank(message = "地块名称不能为空") @Size(max = 100, message = "地块名称长度不能超过100字符") String getLandName() {
        return landName;
    }

    public void setLandName(@NotBlank(message = "地块名称不能为空") @Size(max = 100, message = "地块名称长度不能超过100字符") String landName) {
        this.landName = landName;
    }

    public @NotBlank(message = "地块边界不能为空") @Pattern(regexp = "^([\\d.]+,[-\\d.]+;)+([\\d.]+,[-\\d.]+)$",
            message = "地块边界格式错误，正确格式：lat1,lng1;lat2,lng2;...") String getLandBoundary() {
        return landBoundary;
    }

    public void setLandBoundary(@NotBlank(message = "地块边界不能为空") @Pattern(regexp = "^([\\d.]+,[-\\d.]+;)+([\\d.]+,[-\\d.]+)$",
            message = "地块边界格式错误，正确格式：lat1,lng1;lat2,lng2;...") String landBoundary) {
        this.landBoundary = landBoundary;
    }

    public @NotBlank(message = "作物类型不能为空") @Size(max = 50, message = "作物类型长度不能超过50字符") String getCropType() {
        return cropType;
    }

    public void setCropType(@NotBlank(message = "作物类型不能为空") @Size(max = 50, message = "作物类型长度不能超过50字符") String cropType) {
        this.cropType = cropType;
    }

    public @Size(max = 100, message = "病虫害类型长度不能超过100字符") String getPestType() {
        return pestType;
    }

    public void setPestType(@Size(max = 100, message = "病虫害类型长度不能超过100字符") String pestType) {
        this.pestType = pestType;
    }
    
    public @Size(max = 200, message = "巡检目的长度不能超过200字符") String getInspectionPurpose() {
        return inspectionPurpose;
    }
    
    public void setInspectionPurpose(@Size(max = 200, message = "巡检目的长度不能超过200字符") String inspectionPurpose) {
        this.inspectionPurpose = inspectionPurpose;
    }

    public @Min(value = 1, message = "期望分辨率必须大于0") @Max(value = 50, message = "期望分辨率不能超过50cm") String getExpectedResolution() {
        return expectedResolution;
    }

    public void setExpectedResolution(@Min(value = 1, message = "期望分辨率必须大于0") @Max(value = 50, message = "期望分辨率不能超过50cm") String expectedResolution) {
        this.expectedResolution = expectedResolution;
    }

    public @NotBlank(message = "地块位置不能为空") @Size(max = 200, message = "地块位置长度不能超过200字符") String getLandLocation() {
        return landLocation;
    }

    public void setLandLocation(@NotBlank(message = "地块位置不能为空") @Size(max = 200, message = "地块位置长度不能超过200字符") String landLocation) {
        this.landLocation = landLocation;
    }

    public @NotNull(message = "地块面积不能为空") @Positive(message = "地块面积必须大于0") Double getLandArea() {
        return landArea;
    }

    public void setLandArea(@NotNull(message = "地块面积不能为空") @Positive(message = "地块面积必须大于0") Double landArea) {
        this.landArea = landArea;
    }

    public @NotNull(message = "期望作业时间不能为空") @Future(message = "期望作业时间必须为未来时间") LocalDateTime getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(@NotNull(message = "期望作业时间不能为空") @Future(message = "期望作业时间必须为未来时间") LocalDateTime expectedTime) {
        this.expectedTime = expectedTime;
    }

    public @NotNull(message = "预算金额不能为空") @Positive(message = "预算金额必须大于0") Double getBudget() {
        return budget;
    }

    public void setBudget(@NotNull(message = "预算金额不能为空") @Positive(message = "预算金额必须大于0") Double budget) {
        this.budget = budget;
    }

    public @Size(max = 500, message = "特殊要求长度不能超过500字符") String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(@Size(max = 500, message = "特殊要求长度不能超过500字符") String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }
    
    public @NotNull(message = "订单类型不能为空") @Min(value = 1, message = "订单类型无效") @Max(value = 2, message = "订单类型无效") Integer getOrderType() {
        return orderType;
    }
    
    public void setOrderType(@NotNull(message = "订单类型不能为空") @Min(value = 1, message = "订单类型无效") @Max(value = 2, message = "订单类型无效") Integer orderType) {
        this.orderType = orderType;
    }
    
    public Long getFarmerId() {
        return farmerId;
    }
    
    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }
    
    public Long getLandId() {
        return landId;
    }
    
    public void setLandId(Long landId) {
        this.landId = landId;
    }
}
