package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.entity.DeviceActivity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备动态Mapper接口
 */
public interface DeviceActivityMapper extends BaseMapper<DeviceActivity> {

    /**
     * 分页查询最近设备动态
     */
    IPage<DeviceActivity> selectRecentActivities(Page<DeviceActivity> page,
                                                @Param("deviceId") Long deviceId,
                                                @Param("activityTypes") List<Integer> activityTypes);

    /**
     * 查询指定设备的最近动态
     */
    List<DeviceActivity> selectDeviceRecentActivities(@Param("deviceId") Long deviceId,
                                                     @Param("limit") Integer limit);

    /**
     * 查询用户相关设备的动态
     */
    IPage<DeviceActivity> selectUserRelatedActivities(Page<DeviceActivity> page,
                                                     @Param("userId") Long userId,
                                                     @Param("limit") Integer limit);
}