package com.cty.nopersonfinally.pojo.vo;

import com.cty.nopersonfinally.pojo.entity.DeviceMaintainRecord;
import lombok.Data;

import java.util.List;

/**
 * 无人机设备详情VO
 */
@Data
public class DeviceDetailVO {
    /**
     * 设备基本信息
     */
    private DroneDeviceBasicInfo deviceInfo;
    
    /**
     * 机主信息
     */
    private OwnerInfo ownerInfo;
    
    /**
     * 最近维护记录
     */
    private DeviceMaintainVO lastMaintainRecord;
    
    /**
     * 维护记录列表
     */
    private List<DeviceMaintainVO> maintainRecords;
    
    /**
     * 设备基本信息内部类
     */
    @Data
    public static class DroneDeviceBasicInfo {
        private Long deviceId;
        private Long ownerId;
        private Long flyerId;
        private String model;
        private String deviceName;
        private String serialNumber;
        private String deviceType;
        private Integer status;
        private String statusDesc;
        private String manufacturer;// 设备制造商
        private String purchaseTime;
        private String lastMaintainTime;
        private Integer isQualified;
        private Double loadCapacity;
        private Double maxLoad;
        private String endurance;
        private String picture; // 设备图片
        private Integer rental_status; // 设备租借状态：0-可租借 1-已租借 2-审核中


        public Integer getRental_status() {
            return rental_status;
        }

        public void setRental_status(Integer rental_status) {
            this.rental_status = rental_status;
        }

        public String getEndurance() {
            return endurance;
        }

        public void setEndurance(String endurance) {
            this.endurance = endurance;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public Double getMaxLoad() {
            return maxLoad;
        }

        public void setMaxLoad(Double maxLoad) {
            this.maxLoad = maxLoad;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }

        public Long getFlyerId() {
            return flyerId;
        }

        public void setFlyerId(Long flyerId) {
            this.flyerId = flyerId;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getStatusDesc() {
            return statusDesc;
        }

        public void setStatusDesc(String statusDesc) {
            this.statusDesc = statusDesc;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public String getPurchaseTime() {
            return purchaseTime;
        }

        public void setPurchaseTime(String purchaseTime) {
            this.purchaseTime = purchaseTime;
        }

        public String getLastMaintainTime() {
            return lastMaintainTime;
        }

        public void setLastMaintainTime(String lastMaintainTime) {
            this.lastMaintainTime = lastMaintainTime;
        }

        public Integer getIsQualified() {
            return isQualified;
        }

        public void setIsQualified(Integer isQualified) {
            this.isQualified = isQualified;
        }

        public Double getLoadCapacity() {
            return loadCapacity;
        }

        public void setLoadCapacity(Double loadCapacity) {
            this.loadCapacity = loadCapacity;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }
    }


    public DroneDeviceBasicInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DroneDeviceBasicInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public DeviceMaintainVO getLastMaintainRecord() {
        return lastMaintainRecord;
    }

    public void setLastMaintainRecord(DeviceMaintainVO lastMaintainRecord) {
        this.lastMaintainRecord = lastMaintainRecord;
    }

    public List<DeviceMaintainVO> getMaintainRecords() {
        return maintainRecords;
    }

    public void setMaintainRecords(List<DeviceMaintainVO> maintainRecords) {
        this.maintainRecords = maintainRecords;
    }

    public OwnerInfo getOwnerInfo() {
        return ownerInfo;
    }

    public void setOwnerInfo(OwnerInfo ownerInfo) {
        this.ownerInfo = ownerInfo;
    }

    /**
     * 机主信息内部类
     */
    @Data
    public static class OwnerInfo {
        private String realName; // 机主姓名
        private String phone; // 机主电话

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

}