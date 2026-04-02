package com.cty.nopersonfinally.pojo.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 巡检报告实体类
 * 存储无人机巡检后的报告数据
 */
@Data
@TableName("inspection_report")
public class InspectionReport {

    /**
     * 报告ID
     */
    @TableId(type = IdType.AUTO)
    private Long reportId;

    /**
     * 关联订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 执行飞手ID
     */
    @TableField("flyer_id")
    private Long flyerId;

    /**
     * 地块ID
     */
    @TableField("land_id")
    private Long landId;

    /**
     * 地块名称
     */
    @TableField("land_name")
    private String landName;

    /**
     * 地块边界（经纬度集合，格式：lat1,lng1;lat2,lng2）
     */
    @TableField("land_boundary")
    private String landBoundary;

    /**
     * 正射影像图OSS地址
     */
    @TableField("orthophoto_url")
    private String orthophotoUrl;

    /**
     * NDVI植被指数图OSS地址
     */
    @TableField("ndvi_url")
    private String ndviUrl;

    /**
     * 病虫害热力图OSS地址
     */
    @TableField("pest_heatmap_url")
    private String pestHeatmapUrl;

    /**
     * 地块总面积（平方米）
     */
    @TableField("land_area")
    private Double landArea;

    /**
     * 作物类型
     */
    @TableField("crop_type")
    private String cropType;

    /**
     * 主要病虫害类型
     */
    @TableField("pest_type")
    private String pestType;

    /**
     * 病虫害发生程度（轻/中/重）
     */
    @TableField("pest_severity")
    private String pestSeverity;

    /**
     * 病虫害防治建议
     */
    @TableField("pest_suggestion")
    private String pestSuggestion;

    /**
     * 巡检日期
     */
    @TableField("inspection_date")
    private LocalDateTime inspectionDate;

    /**
     * 报告状态
     * DRAFT-草稿，SUBMITTED-已提交，CONFIRMED-农户已确认
     */
    @TableField("status")
    private String status;

    /**
     * 农户确认时间
     */
    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    /**
     * 报告创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 报告更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableField("is_deleted")
    private Integer isDeleted;


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

    public Long getFlyerId() {
        return flyerId;
    }

    public void setFlyerId(Long flyerId) {
        this.flyerId = flyerId;
    }

    public Long getLandId() {
        return landId;
    }

    public void setLandId(Long landId) {
        this.landId = landId;
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

    public Double getLandArea() {
        return landArea;
    }

    public void setLandArea(Double landArea) {
        this.landArea = landArea;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getConfirmTime() {
        return confirmTime;
    }

    public void setConfirmTime(LocalDateTime confirmTime) {
        this.confirmTime = confirmTime;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
