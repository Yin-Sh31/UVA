package com.cty.nopersonfinally.pojo.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 农业数据处理结果DTO
 * 存储影像处理后的各项结果数据
 */
@Data
public class AgricultureDataResult {

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
     * 地块总面积（平方米）
     */
    private Double landArea;

    /**
     * 异常区域面积（平方米）
     */
    private Double abnormalArea;

    /**
     * 异常区域占比（%）
     */
    private Double abnormalRatio;

    /**
     * 主要作物类型
     */
    private String cropType;

    /**
     * 病虫害类型预测
     */
    private String pestType;

    /**
     * 病虫害防治建议
     */
    private String pestSuggestion;

    /**
     * 数据处理状态
     * SUCCESS-成功，PROCESSING-处理中，FAILED-失败
     */
    private String processStatus;

    /**
     * 处理失败时的错误信息
     */
    private String errorMsg;

    /**
     * 数据处理时间
     */
    private LocalDateTime processTime;

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

    public Double getAbnormalArea() {
        return abnormalArea;
    }

    public void setAbnormalArea(Double abnormalArea) {
        this.abnormalArea = abnormalArea;
    }

    public Double getAbnormalRatio() {
        return abnormalRatio;
    }

    public void setAbnormalRatio(Double abnormalRatio) {
        this.abnormalRatio = abnormalRatio;
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

    public String getPestSuggestion() {
        return pestSuggestion;
    }

    public void setPestSuggestion(String pestSuggestion) {
        this.pestSuggestion = pestSuggestion;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }
}
