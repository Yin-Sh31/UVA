// main/java/com/cty/nopersonfinally/pojo/vo/SprayDemandVO.java
package com.cty.nopersonfinally.pojo.vo;

import com.cty.nopersonfinally.pojo.enums.OrderTypeEnum;
import com.cty.nopersonfinally.pojo.enums.SprayDemandStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "喷洒需求展示信息")
public class DemandVO {
    @Schema(description = "需求ID")
    private Long demandId;

    @Schema(description = "农户ID")
    private Long farmerId;

    @Schema(description = "飞手ID（接单后有值）")
    private Long flyerId;

    @Schema(description = "飞手姓名")
    private String flyerName;

    @Schema(description = "农户姓名")
    private String farmerName;

    @Schema(description = "关联巡检报告ID")
    private Long reportId;

    @Schema(description = "地块名称")
    private String landName;
    
    @Schema(description = "地块ID")
    private Long landId;

    @Schema(description = "作物类型")
    private String cropType;

    @Schema(description = "病虫害类型")
    private String pestType;

    @Schema(description = "地块面积（亩）")
    private Double landArea;

    @Schema(description = "预算金额")
    private Double budget;

    @Schema(description = "期望作业时间")
    private LocalDateTime expectedTime;

    @Schema(description = "状态码")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "接单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime acceptTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @Schema(description = "订单类型：1-喷洒，2-巡检")
    private Integer orderType;
    
    @Schema(description = "订单类型描述")
    private String orderTypeDesc;
    
    @Schema(description = "巡检目的")
    private String inspectionPurpose;
    
    @Schema(description = "期望分辨率（cm）")
    private String expectedResolution;

    @Schema(description = "实际支付金额")
    private Double paymentAmount;

    @Schema(description = "支付状态：0-未支付，1-已支付，2-支付失败")
    private Integer paymentStatus;

    @Schema(description = "支付状态描述")
    private String paymentStatusDesc;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime paymentTime;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "地块位置")
    private String landLocation;

    // 转换状态描述
    public void setStatus(Integer status) {
        this.status = status;
        this.statusDesc = SprayDemandStatusEnum.getDescByCode(status);
    }
    
    // 转换支付状态描述
    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
        if (paymentStatus == null) {
            this.paymentStatusDesc = "未支付";
        } else if (paymentStatus == 0) {
            this.paymentStatusDesc = "未支付";
        } else if (paymentStatus == 1) {
            this.paymentStatusDesc = "已支付";
        } else if (paymentStatus == 2) {
            this.paymentStatusDesc = "支付失败";
        } else {
            this.paymentStatusDesc = "未知状态";
        }
    }


    public Long getDemandId() {
        return demandId;
    }

    public void setDemandId(Long demandId) {
        this.demandId = demandId;
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

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getLandName() {
        return landName;
    }

    public void setLandName(String landName) {
        this.landName = landName;
    }
    
    public Long getLandId() {
        return landId;
    }
    
    public void setLandId(Long landId) {
        this.landId = landId;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public String getPestType() {
        return pestType;
    }

    public void setPestType(String pestType) {
        this.pestType = pestType;
    }

    public Double getLandArea() {
        return landArea;
    }

    public void setLandArea(Double landArea) {
        this.landArea = landArea;
    }

    public LocalDateTime getExpectedTime() {
        return expectedTime;
    }

    public void setExpectedTime(LocalDateTime expectedTime) {
        this.expectedTime = expectedTime;
    }

    public Integer getStatus() {
        return status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public LocalDateTime getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(LocalDateTime acceptTime) {
        this.acceptTime = acceptTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public Integer getOrderType() {
        return orderType;
    }
    
    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
        this.orderTypeDesc = orderType != null ? OrderTypeEnum.getDescByCode(orderType) : null;
    }
    
    public String getOrderTypeDesc() {
        return orderTypeDesc;
    }
    
    public void setOrderTypeDesc(String orderTypeDesc) {
        this.orderTypeDesc = orderTypeDesc;
    }
    
    public String getInspectionPurpose() {
        return inspectionPurpose;
    }
    
    public void setInspectionPurpose(String inspectionPurpose) {
        this.inspectionPurpose = inspectionPurpose;
    }

    public String getExpectedResolution() {
        return expectedResolution;
    }

    public void setExpectedResolution(String expectedResolution) {
        this.expectedResolution = expectedResolution;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentStatusDesc() {
        return paymentStatusDesc;
    }

    public void setPaymentStatusDesc(String paymentStatusDesc) {
        this.paymentStatusDesc = paymentStatusDesc;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getLandLocation() {
        return landLocation;
    }

    public void setLandLocation(String landLocation) {
        this.landLocation = landLocation;
    }
}