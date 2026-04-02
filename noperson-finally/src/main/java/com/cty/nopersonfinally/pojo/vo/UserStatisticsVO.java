package com.cty.nopersonfinally.pojo.vo;

import lombok.Data;

@Data
public class UserStatisticsVO {
    private int farmerCount; // 农户数量
    private int flyerCount; // 飞手数量
    private int ownerCount; // 机主数量
    private int pendingAuditCount; // 待审核数量


    public int getFarmerCount() {
        return farmerCount;
    }

    public void setFarmerCount(int farmerCount) {
        this.farmerCount = farmerCount;
    }

    public int getFlyerCount() {
        return flyerCount;
    }

    public void setFlyerCount(int flyerCount) {
        this.flyerCount = flyerCount;
    }

    public int getOwnerCount() {
        return ownerCount;
    }

    public void setOwnerCount(int ownerCount) {
        this.ownerCount = ownerCount;
    }

    public int getPendingAuditCount() {
        return pendingAuditCount;
    }

    public void setPendingAuditCount(int pendingAuditCount) {
        this.pendingAuditCount = pendingAuditCount;
    }
}