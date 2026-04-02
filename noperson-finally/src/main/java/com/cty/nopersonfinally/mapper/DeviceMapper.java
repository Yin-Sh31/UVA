package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 设备Mapper接口
 */
public interface DeviceMapper extends BaseMapper<DroneDevice> {
    
    /**
     * 清除一级缓存
     */
    void clearCache();
    
    /**
     * 分页查询飞手的设备
     */
    IPage<DroneDevice> selectFlyerDevices(Page<DroneDevice> page,
                                          @Param("flyerId") Long flyerId,
                                          @Param("status") Integer status);

    /**
     * 查询符合条件的空闲设备
     * @param type 设备类型
     * @param loadCapacity 最小载重要求
     * @param enduranceTime 最小续航要求
     */
    List<DroneDevice> selectAvailableDevices(@Param("type") Integer type,
                                        @Param("loadCapacity") Double loadCapacity,
                                        @Param("enduranceTime") Integer enduranceTime);

    /**
     * 更新设备的最后维护时间
     */
    int updateLastMaintainTime(@Param("id") Long id, @Param("maintainTime") Date maintainTime);

    /**
     * 根据设备ID查询设备信息
     */
    @Select("SELECT * FROM drone_device WHERE device_id = #{deviceId}")
    DroneDevice getById(Long deviceId);

    IPage<DroneDevice> selectOwnerDevices(Page<DroneDevice> page, Long ownerId, Integer status);

    /**
     * 删除设备
     */
    @Delete("DELETE FROM drone_device WHERE device_id = #{deviceId}")
    boolean deleteDeviceById(Long deviceId);

    @Update("UPDATE drone_device SET rental_status = 1, flyer_id = #{flyerId} WHERE device_id = #{deviceId}")
    int updateById(@Param("deviceId") Long deviceId, @Param("flyerId") Long flyerId);
    /**
     * 更新设备信息
     */
    boolean OwnerUpdateDeviceById(DroneDevice device);
}
