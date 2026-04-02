// main/java/com/cty/nopersonfinally/pojo/vo/InspectionOrderVO.java
package com.cty.nopersonfinally.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "巡检订单展示信息")
public class InspectionOrderVO {
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "农户ID")
    private Long farmerId;

    @Schema(description = "飞手ID（匹配后才有值）")
    private Long flyerId;

    @Schema(description = "飞手姓名（匹配后才有值）")
    private String flyerName;

    @Schema(description = "地块名称")
    private String landName;

    @Schema(description = "地块边界（经纬度集合）")
    private String landBoundary;

    @Schema(description = "巡检目的（1-病虫害检测 2-生长情况评估 3-其他）")
    private Integer inspectionPurpose;

    @Schema(description = "巡检目的描述")
    private String inspectionPurposeDesc;

    @Schema(description = "期望分辨率")
    private String expectedResolution;

    @Schema(description = "期望巡检时间")
    private LocalDate expectedTime;

    @Schema(description = "预算金额")
    private Double budget;

    @Schema(description = "订单状态（0-待匹配，1-处理中，2-作业中，3-待确认，4-已完成，5-已取消）")
    private String status;

    @Schema(description = "订单状态描述")
    private String statusDesc;

    @Schema(description = "订单创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "订单更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // 转换巡检目的为描述文字
    public void setInspectionPurpose(Integer inspectionPurpose) {
        this.inspectionPurpose = inspectionPurpose;
        switch (inspectionPurpose) {
            case 1:
                this.inspectionPurposeDesc = "病虫害检测";
                break;
            case 2:
                this.inspectionPurposeDesc = "生长情况评估";
                break;
            case 3:
                this.inspectionPurposeDesc = "其他";
                break;
            default:
                this.inspectionPurposeDesc = "未知";
        }
    }

    // 转换状态为描述文字（适配Integer类型的status）
    public void setStatus(String status) {
        this.status = status;
        try {
            int code = Integer.parseInt(status);
            switch (code) {
                case 0:
                    this.statusDesc = "待接取";
                    break;
                case 1:
                    this.statusDesc = "处理中";
                    break;
                case 2:
                    this.statusDesc = "作业中";
                    break;
                case 3:
                    this.statusDesc = "待确认";
                    break;
                case 4:
                    this.statusDesc = "已完成";
                    break;
                case 5:
                    this.statusDesc = "已取消";
                    break;
                default:
                    this.statusDesc = "未知状态";
            }
        } catch (NumberFormatException e) {
            this.statusDesc = "未知状态";
        }
    }


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

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

    public String getLandName() {
        return landName;
    }

    public void setLandName(String landName) {
        this.landName = landName;
    }

    public String getLandBoundary() {
        return landBoundary;
    }

    public void setLandBoundary(String landBoundary) {
        this.landBoundary = landBoundary;
    }

    public Integer getInspectionPurpose() {
        return inspectionPurpose;
    }

    public String getInspectionPurposeDesc() {
        return inspectionPurposeDesc;
    }

    public void setInspectionPurposeDesc(String inspectionPurposeDesc) {
        this.inspectionPurposeDesc = inspectionPurposeDesc;
    }

    public String getExpectedResolution() {
        return expectedResolution;
    }

    public void setExpectedResolution(String expectedResolution) {
        this.expectedResolution = expectedResolution;
    }

    public LocalDate getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(LocalDate expectedTime) {
        this.expectedTime = expectedTime;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}