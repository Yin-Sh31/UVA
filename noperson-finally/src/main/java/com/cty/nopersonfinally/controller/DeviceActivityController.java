package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.DeviceActivity;
import com.cty.nopersonfinally.service.DeviceActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备动态Controller
 */
@RestController
@RequestMapping("/device-activities")
@Api(tags = "设备动态管理")
public class DeviceActivityController {

    @Autowired
    private DeviceActivityService deviceActivityService;

    /**
     * 获取最近设备动态
     */
    @ApiOperation("获取最近设备动态")
    @GetMapping("/recent")
    public Result<IPage<DeviceActivity>> getRecentActivities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) List<Integer> activityTypes) {
        IPage<DeviceActivity> page = deviceActivityService.getRecentActivities(pageNum, pageSize, deviceId, activityTypes);
        return Result.ok(page);
    }

    /**
     * 获取指定设备的最近动态
     */
    @ApiOperation("获取指定设备的最近动态")
    @GetMapping("/device/{deviceId}")
    public Result<List<DeviceActivity>> getDeviceActivities(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<DeviceActivity> activities = deviceActivityService.getDeviceRecentActivities(deviceId, limit);
        return Result.ok(activities);
    }

    /**
     * 获取当前用户相关设备的动态
     */
    @ApiOperation("获取当前用户相关设备的动态")
    @GetMapping("/my-related")
    public Result<List<DeviceActivity>> getUserRelatedActivities(
            @RequestParam(defaultValue = "20") Integer limit) {
        // 获取当前登录用户ID
        Long userId = getCurrentUserId();
        List<DeviceActivity> activities = deviceActivityService.getUserRelatedActivities(userId, limit);
        return Result.ok(activities);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return Long.parseLong(((UserDetails) principal).getUsername());
        }
        return Long.parseLong(principal.toString());
    }
}