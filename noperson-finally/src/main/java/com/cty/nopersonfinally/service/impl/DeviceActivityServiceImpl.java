package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.DeviceActivityMapper;
import com.cty.nopersonfinally.pojo.entity.DeviceActivity;
import com.cty.nopersonfinally.service.DeviceActivityService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备动态Service实现类
 */
@Service
public class DeviceActivityServiceImpl extends ServiceImpl<DeviceActivityMapper, DeviceActivity>
        implements DeviceActivityService {

    @Override
    public void recordDeviceActivity(Long deviceId, Integer activityType, String activityDesc,
                                    Long operatorId, String operatorName,
                                    Integer beforeStatus, Integer afterStatus) {
        DeviceActivity activity = new DeviceActivity();
        activity.setDeviceId(deviceId);
        activity.setActivityType(activityType);
        activity.setActivityDesc(activityDesc);
        activity.setOperatorId(operatorId);
        activity.setOperatorName(operatorName);
        activity.setBeforeStatus(beforeStatus);
        activity.setAfterStatus(afterStatus);
        activity.setCreateTime(LocalDateTime.now());
        this.save(activity);
    }

    @Override
    public IPage<DeviceActivity> getRecentActivities(Integer pageNum, Integer pageSize,
                                                   Long deviceId, List<Integer> activityTypes) {
        Page<DeviceActivity> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectRecentActivities(page, deviceId, activityTypes);
    }

    @Override
    public List<DeviceActivity> getDeviceRecentActivities(Long deviceId, Integer limit) {
        return baseMapper.selectDeviceRecentActivities(deviceId, limit);
    }

    @Override
    public List<DeviceActivity> getUserRelatedActivities(Long userId, Integer limit) {
        Page<DeviceActivity> page = new Page<>(1, limit);
        IPage<DeviceActivity> result = baseMapper.selectUserRelatedActivities(page, userId, limit);
        return result.getRecords();
    }

    @Override
    public void batchRecordActivities(List<DeviceActivity> activities) {
        for (DeviceActivity activity : activities) {
            activity.setCreateTime(LocalDateTime.now());
        }
        this.saveBatch(activities);
    }
}