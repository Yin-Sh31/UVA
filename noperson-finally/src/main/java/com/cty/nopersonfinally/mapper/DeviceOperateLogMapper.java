package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.entity.DeviceOperateLog;
import org.apache.ibatis.annotations.Param;

/**
 * 设备操作日志Mapper接口
 */
public interface DeviceOperateLogMapper extends BaseMapper<DeviceOperateLog> {

    /**
     * 分页查询设备操作日志
     */
    IPage<DeviceOperateLog> selectDeviceLogs(Page<DeviceOperateLog> page,
                                             @Param("deviceId") Long deviceId,
                                             @Param("startTime") String startTime,
                                             @Param("endTime") String endTime,
                                             @Param("operateType") Integer operateType);
}
