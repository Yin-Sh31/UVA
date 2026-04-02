package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.entity.DeviceActivity;

import java.util.List;

/**
 * 设备动态Service接口
 */
public interface DeviceActivityService extends IService<DeviceActivity> {

    /**
     * 记录设备动态
     * @param deviceId 设备ID
     * @param activityType 动态类型
     * @param activityDesc 动态描述
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param beforeStatus 变更前状态
     * @param afterStatus 变更后状态
     */
    void recordDeviceActivity(Long deviceId, Integer activityType, String activityDesc,
                            Long operatorId, String operatorName,
                            Integer beforeStatus, Integer afterStatus);

    /**
     * 获取最近设备动态
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param deviceId 设备ID（可选）
     * @param activityTypes 动态类型列表（可选）
     */
    IPage<DeviceActivity> getRecentActivities(Integer pageNum, Integer pageSize,
                                            Long deviceId, List<Integer> activityTypes);

    /**
     * 获取指定设备的最近动态
     * @param deviceId 设备ID
     * @param limit 限制数量
     */
    List<DeviceActivity> getDeviceRecentActivities(Long deviceId, Integer limit);

    /**
     * 获取用户相关设备的动态
     * @param userId 用户ID
     * @param limit 限制数量
     */
    List<DeviceActivity> getUserRelatedActivities(Long userId, Integer limit);

    /**
     * 批量记录设备动态
     * @param activities 设备动态列表
     */
    void batchRecordActivities(List<DeviceActivity> activities);
}