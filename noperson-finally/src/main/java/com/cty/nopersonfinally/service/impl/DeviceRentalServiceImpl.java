package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.DeviceRentalMapper;
import com.cty.nopersonfinally.pojo.entity.DeviceRental;
import com.cty.nopersonfinally.service.DeviceRentalService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * 设备租借记录服务实现类
 */
@Service
public class DeviceRentalServiceImpl extends ServiceImpl<DeviceRentalMapper, DeviceRental> implements DeviceRentalService {
    
    @Resource
    private DeviceRentalMapper deviceRentalMapper;
    
    @Override
    @Transactional
    public DeviceRental createRentalRecord(Long deviceId, Long flyerId, Long ownerId) {
        DeviceRental rental = new DeviceRental();
        rental.setDeviceId(deviceId);
        rental.setFlyerId(flyerId);
        rental.setOwnerId(ownerId);
        rental.setRentalStartTime(new Date());
        rental.setRentalStatus(1); // 1-租借中
        rental.setPaymentStatus(0); // 0-未支付
        
        // 插入租借记录
        save(rental);
        
        return rental;
    }
    
    @Override
    @Transactional
    public boolean completeReturn(Long rentalId) {
        return deviceRentalMapper.updateRentalStatus(rentalId, 2, new Date()) > 0; // 2-已归还
    }
    
    @Override
    public DeviceRental getCurrentRentalByDeviceId(Long deviceId) {
        return deviceRentalMapper.getCurrentRentalByDeviceId(deviceId);
    }
    
    @Override
    public List<DeviceRental> getRentalHistoryByFlyerId(Long flyerId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认查询10条
        }
        return deviceRentalMapper.getRentalHistoryByFlyerId(flyerId, limit);
    }
    
    @Override
    public List<DeviceRental> getRentalHistoryByDeviceId(Long deviceId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认查询10条
        }
        return deviceRentalMapper.getRentalHistoryByDeviceId(deviceId, limit);
    }
    
    @Override
    public DeviceRental getActiveRental(Long deviceId, Long flyerId) {
        // 使用QueryWrapper查询飞手和设备的有效租借记录（状态为1-租借中）
        QueryWrapper<DeviceRental> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId)
                   .eq("flyer_id", flyerId)
                   .eq("rental_status", 1); // 1表示租借中
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        
        return deviceRentalMapper.selectOne(queryWrapper);
    }
}