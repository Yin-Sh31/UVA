// OrderStatisticsVO.java - 订单统计VO
package com.cty.nopersonfinally.pojo.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OrderStatisticsVO {
    private int totalInspection; // 巡检订单总数
    private int totalSpray; // 喷洒订单总数
    private int pendingOrders; // 待处理订单总数
    private Map<String, Integer> inspectionStatus = new HashMap<>(); // 巡检订单状态分布
    private Map<String, Integer> sprayStatus = new HashMap<>(); // 喷洒订单状态分布


    public int getTotalInspection() {
        return totalInspection;
    }

    public void setTotalInspection(int totalInspection) {
        this.totalInspection = totalInspection;
    }

    public int getTotalSpray() {
        return totalSpray;
    }

    public void setTotalSpray(int totalSpray) {
        this.totalSpray = totalSpray;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Map<String, Integer> getInspectionStatus() {
        return inspectionStatus;
    }

    public void setInspectionStatus(Map<String, Integer> inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }

    public Map<String, Integer> getSprayStatus() {
        return sprayStatus;
    }

    public void setSprayStatus(Map<String, Integer> sprayStatus) {
        this.sprayStatus = sprayStatus;
    }
}