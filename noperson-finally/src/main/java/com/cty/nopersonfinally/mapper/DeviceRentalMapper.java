package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.DeviceRental;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 设备租借记录Mapper接口
 */
public interface DeviceRentalMapper extends BaseMapper<DeviceRental> {
    
    /**
     * 根据设备ID获取当前租借记录
     */
    DeviceRental getCurrentRentalByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 根据飞手ID获取租借历史记录
     */
    List<DeviceRental> getRentalHistoryByFlyerId(@Param("flyerId") Long flyerId, @Param("limit") Integer limit);
    
    /**
     * 根据设备ID获取租借历史记录
     */
    List<DeviceRental> getRentalHistoryByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") Integer limit);
    
    /**
     * 更新租借记录状态
     */
    int updateRentalStatus(@Param("rentalId") Long rentalId, @Param("status") Integer status, 
                          @Param("endTime") java.util.Date endTime);
}