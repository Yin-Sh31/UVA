package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.DeviceRental;
import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.service.DeviceRentalService;
import com.cty.nopersonfinally.service.DeviceService;

import jakarta.annotation.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备租借记录控制器
 */
@RestController
@RequestMapping("/rental")
public class DeviceRentalController {
    
    @Resource
    private DeviceRentalService deviceRentalService;
    
    @Resource
    private DeviceService deviceService;
    
    /**
     * 获取当前飞手的租借历史记录
     * GET /rental/my-history?limit=10
     */
    @GetMapping("/my-history")
    public Result<List<DeviceRental>> getMyRentalHistory(@RequestParam(defaultValue = "10") Integer limit, Principal principal) {
        Long flyerId = Long.parseLong(principal.getName());
        List<DeviceRental> history = deviceRentalService.getRentalHistoryByFlyerId(flyerId, limit);
        return Result.ok(history);
    }
    
    /**
     * 获取设备的租借历史记录
     * GET /rental/device-history/{deviceId}?limit=10
     */
    @GetMapping("/device-history/{deviceId}")
    public Result<List<DeviceRental>> getDeviceRentalHistory(
            @PathVariable Long deviceId, 
            @RequestParam(defaultValue = "10") Integer limit) {
        List<DeviceRental> history = deviceRentalService.getRentalHistoryByDeviceId(deviceId, limit);
        return Result.ok(history);
    }
    
    /**
     * 获取设备当前的租借状态
     * GET /rental/current/{deviceId}
     */
    @GetMapping("/current/{deviceId}")
    public Result<DeviceRental> getCurrentRental(@PathVariable Long deviceId) {
        DeviceRental rental = deviceRentalService.getCurrentRentalByDeviceId(deviceId);
        return Result.ok(rental);
    }
    
    /**
     * 查询我的活跃租借（进行中）
     * GET /rental/active
     */
    @GetMapping("/active")
    public Result<List<Map<String, Object>>> getActiveRentals(Principal principal) {
        Long flyerId = Long.parseLong(principal.getName());
        // 获取飞手的所有租借记录
        List<DeviceRental> rentals = deviceRentalService.getRentalHistoryByFlyerId(flyerId, null);
        // 过滤出状态为进行中的记录
        rentals.removeIf(rental -> rental.getRentalStatus() != 1);
        
        // 获取所有设备ID
        List<Long> deviceIds = rentals.stream()
                .map(DeviceRental::getDeviceId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询设备信息
        List<DroneDevice> devices = new ArrayList<>();
        for (Long deviceId : deviceIds) {
            DroneDevice device = deviceService.getById(deviceId);
            if (device != null) {
                devices.add(device);
            }
        }
        
        // 将设备信息映射为Map，便于查找
        Map<Long, DroneDevice> deviceMap = devices.stream()
                .collect(Collectors.toMap(DroneDevice::getDeviceId, device -> device));
        
        // 组装返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceRental rental : rentals) {
            Map<String, Object> item = new java.util.HashMap<>();
            // 添加租借记录信息
            item.put("rentalId", rental.getRentalId());
            item.put("deviceId", rental.getDeviceId());
            item.put("ownerId", rental.getOwnerId());
            item.put("flyerId", rental.getFlyerId());
            item.put("rentalStartTime", rental.getRentalStartTime());
            item.put("rentalEndTime", rental.getRentalEndTime());
            item.put("rentalStatus", rental.getRentalStatus());
            item.put("rentalAmount", rental.getRentalAmount());
            item.put("paymentStatus", rental.getPaymentStatus());
            item.put("paymentTime", rental.getPaymentTime());
            item.put("createTime", rental.getCreateTime());
            item.put("updateTime", rental.getUpdateTime());
            item.put("remark", rental.getRemark());
            
            // 添加设备信息
            DroneDevice device = deviceMap.get(rental.getDeviceId());
            if (device != null) {
                item.put("deviceName", device.getDeviceName());
                item.put("model", device.getModel());
                item.put("deviceType", device.getDeviceType());
                item.put("picture", device.getPicture());
            } else {
                item.put("deviceName", "未知设备");
                item.put("model", "未知型号");
                item.put("deviceType", "未知类型");
                item.put("picture", "");
            }
            
            result.add(item);
        }
        
        return Result.ok(result);
    }
    
    /**
     * 获取设备动态记录（分页查询所有租赁记录）
     * GET /rental/dynamics?pageNum=1&pageSize=10
     */
    @GetMapping("/dynamics")
    public Result<Page<DeviceRental>> getDeviceDynamics(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<DeviceRental> page = new Page<>(pageNum, pageSize);
        // 使用MyBatis-Plus的分页查询，按创建时间倒序
        Page<DeviceRental> rentalPage = deviceRentalService.page(page, 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeviceRental>()
                        .orderByDesc("create_time"));
        return Result.ok(rentalPage);
    }
}