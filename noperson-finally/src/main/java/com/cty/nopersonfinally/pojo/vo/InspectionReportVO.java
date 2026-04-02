package com.cty.nopersonfinally.pojo.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检报告前端展示VO
 * 用于向前端返回巡检报告的展示数据
 */
@Data
public class InspectionReportVO {

    /**
     * 报告ID
     */
    private Long reportId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 地块名称
     */
    private String landName;

    /**
     * 正射影像图OSS地址
     */
    private String orthophotoUrl;

    /**
     * NDVI植被指数图OSS地址
     */
    private String ndviUrl;

    /**
     * 病虫害热力图OSS地址
     */
    private String pestHeatmapUrl;

    /**
     * 地块总面积（亩）
     */
    private Double landAreaMu;

    /**
     * 作物类型
     */
    private String cropType;

    /**
     * 主要病虫害类型
     */
    private String pestType;

    /**
     * 病虫害发生程度（轻/中/重）
     */
    private String pestSeverity;

    /**
     * 病虫害防治建议
     */
    private String pestSuggestion;

    /**
     * 巡检日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime inspectionDate;

    /**
     * 报告状态（文字描述）
     */
    private String statusDesc;

    /**
     * 执行飞手姓名
     */
    private String flyerName;

    /**
     * 飞手联系电话
     */
    private String flyerPhone;

    /**
     * 报告创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;


    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getLandName() {
        return landName;
    }

    public void setLandName(String landName) {
        this.landName = landName;
    }

    public String getOrthophotoUrl() {
        return orthophotoUrl;
    }

    public void setOrthophotoUrl(String orthophotoUrl) {
        this.orthophotoUrl = orthophotoUrl;
    }

    public String getNdviUrl() {
        return ndviUrl;
    }

    public void setNdviUrl(String ndviUrl) {
        this.ndviUrl = ndviUrl;
    }

    public String getPestHeatmapUrl() {
        return pestHeatmapUrl;
    }

    public void setPestHeatmapUrl(String pestHeatmapUrl) {
        this.pestHeatmapUrl = pestHeatmapUrl;
    }

    public Double getLandAreaMu() {
        return landAreaMu;
    }

    public void setLandAreaMu(Double landAreaMu) {
        this.landAreaMu = landAreaMu;
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

    public String getPestSeverity() {
        return pestSeverity;
    }

    public void setPestSeverity(String pestSeverity) {
        this.pestSeverity = pestSeverity;
    }

    public String getPestSuggestion() {
        return pestSuggestion;
    }

    public void setPestSuggestion(String pestSuggestion) {
        this.pestSuggestion = pestSuggestion;
    }

    public LocalDateTime getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(LocalDateTime inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getFlyerName() {
        return flyerName;
    }

    public void setFlyerName(String flyerName) {
        this.flyerName = flyerName;
    }

    public String getFlyerPhone() {
        return flyerPhone;
    }

    public void setFlyerPhone(String flyerPhone) {
        this.flyerPhone = flyerPhone;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}


