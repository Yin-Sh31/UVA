package com.cty.nopersonfinally.pojo.vo;

import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import lombok.Data;

import java.util.List;

/**
 * 机主详情信息VO
 */
@Data
public class OwnerInfoVO {
    /**
     * 机主用户ID
     */
    private Long userId;
    
    /**
     * 公司名称（使用real_name字段）
     */
    private String companyName;
    
    /**
     * 设备总数
     */
    private Integer deviceTotal;
    
    /**
     * 可用设备数量
     */
    private Integer availableDeviceCount;
    
    /**
     * 设备列表
     */
    private List<DroneDevice> deviceList;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getDeviceTotal() {
        return deviceTotal;
    }

    public void setDeviceTotal(Integer deviceTotal) {
        this.deviceTotal = deviceTotal;
    }

    public Integer getAvailableDeviceCount() {
        return availableDeviceCount;
    }

    public void setAvailableDeviceCount(Integer availableDeviceCount) {
        this.availableDeviceCount = availableDeviceCount;
    }

    public List<DroneDevice> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<DroneDevice> deviceList) {
        this.deviceList = deviceList;
    }
}