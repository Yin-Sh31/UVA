package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.enums.DeviceStatusEnum;
import com.cty.nopersonfinally.pojo.vo.DeviceDetailVO;
import com.cty.nopersonfinally.service.DeviceService;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/device")
@Api(tags = "设备管理")
public class DeviceController {
    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    @Resource
    private DeviceService deviceService;
    
    @Resource
    private SysUserMapper sysUserMapper;



    /**
     * 测试端点 - 用于直接验证endurance字段的序列化
     * 这个端点会返回一个包含endurance字段的测试数据对象
     */
    @GetMapping("/test-endurance")
    @ApiOperation("测试endurance字段序列化")
    public Result<?> testEnduranceSerialization() {
        try {
            // 创建测试设备信息对象
            DeviceDetailVO testVO = new DeviceDetailVO();
            DeviceDetailVO.DroneDeviceBasicInfo basicInfo = new DeviceDetailVO.DroneDeviceBasicInfo();
            
            // 设置测试数据，确保包含endurance字段
            basicInfo.setDeviceId(1L);
            basicInfo.setDeviceName("测试无人机");
            basicInfo.setModel("Mavic 3");
            basicInfo.setEndurance("45"); // 设置明确的endurance值为45分钟
            basicInfo.setMaxLoad(2.0);
            basicInfo.setDeviceType("多旋翼");
            
            testVO.setDeviceInfo(basicInfo);
            
            // 记录日志
            log.info("测试endurance序列化，值为: {}", basicInfo.getEndurance());
            
            // 直接返回，测试序列化
            return Result.ok(testVO);
        } catch (Exception e) {
            log.error("测试endurance序列化失败: {}", e.getMessage(), e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试端点 - 返回完整的设备列表测试数据
     * 模拟getAllDevices的返回结构，但使用硬编码的数据
     */
    @GetMapping("/test-list")
    @ApiOperation("测试设备列表返回结构")
    public Result<?> testDeviceList() {
        try {
            // 创建分页对象
            Page<DeviceDetailVO> page = new Page<>(1, 10);
            
            // 创建测试数据列表
            List<DeviceDetailVO> records = new ArrayList<>();
            
            // 添加3条测试数据
            for (int i = 1; i <= 3; i++) {
                DeviceDetailVO device = new DeviceDetailVO();
                DeviceDetailVO.DroneDeviceBasicInfo basicInfo = new DeviceDetailVO.DroneDeviceBasicInfo();
                
                basicInfo.setDeviceId((long)i);
                basicInfo.setDeviceName("测试设备" + i);
                basicInfo.setModel("测试型号" + i);
                basicInfo.setEndurance(""+30 + i * 5); // endurance值分别为35, 40, 45
                basicInfo.setMaxLoad(1.0 + i * 0.5);
                
                device.setDeviceInfo(basicInfo);
                records.add(device);
            }
            
            page.setRecords(records);
            page.setTotal(records.size());
            page.setCurrent(1);
            page.setSize(10);
            
            log.info("测试设备列表，第一条设备endurance值: {}", records.get(0).getDeviceInfo().getEndurance());
            
            return Result.ok(page);
        } catch (Exception e) {
            log.error("测试设备列表失败: {}", e.getMessage(), e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有可用设备（状态正常且未被租借）
     * @return 可用设备列表
     */
    @ApiOperation("获取可用设备列表")
    @GetMapping("/available")
    @PreAuthorize("hasRole('flyer')")
    public Result<List<DroneDevice>> getAvailableDevices() {
        List<DroneDevice> devices = deviceService.getAvailableDevices();
        return Result.ok(devices);
    }

    /**
     * 检查设备是否可租借
     * @param deviceId 设备ID
     * @return 检查结果
     */
    @ApiOperation("检查设备是否可租借")
    @GetMapping("/check-available/{deviceId}")
    @PreAuthorize("hasRole('flyer')")
    public Result<Boolean> checkDeviceAvailable(@PathVariable Long deviceId) {
        boolean available = deviceService.checkDeviceAvailable(deviceId);
        return Result.ok(available);
    }

    /**
     * 租借无人机
     * @param deviceId 设备ID
     * @param principal 当前登录用户
     * @return 租借结果
     */
    @ApiOperation("租借无人机")
    @PostMapping("/rent/{deviceId}")
    @PreAuthorize("hasRole('flyer')")
    public Result rentDevice(@PathVariable Long deviceId, Principal principal) {
        try {
            log.info("====== 收到设备租借请求 ======");
            log.info("设备ID: {}", deviceId);

            Long flyerId = Long.parseLong(principal.getName());
            log.info("飞手ID: {}", flyerId);

            // 首先检查设备是否可租借
            boolean isAvailable = deviceService.checkDeviceAvailable(deviceId);
            log.info("设备可租借检查结果: {}", isAvailable);

            // 调用服务层执行租借操作
            log.info("开始调用deviceService.rentDevice");
            boolean success = deviceService.rentDevice(deviceId, flyerId);
            log.info("deviceService.rentDevice返回结果: {}", success);

            if (success) {
                // 租借成功后，再次查询设备状态以验证
                DroneDevice updatedDevice = deviceService.getById(deviceId);
                if (updatedDevice != null) {
                    log.info("租借后设备状态验证: flyerId={}, rentalStatus={}",
                            updatedDevice.getFlyerId(), updatedDevice.getRentalStatus());
                }
                log.info("设备租借成功，返回成功响应");
                return Result.ok("设备租借成功");
            } else {
                log.warn("设备租借失败，返回失败响应");
                return Result.error("设备租借失败，请检查设备是否可租借");
            }
        } catch (Exception e) {
            log.error("设备租借过程中发生异常: {}", e.getMessage(), e);
            return Result.error("设备租借过程中发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 取消设备租借
     * @param deviceId 设备ID
     * @param principal 当前登录用户
     * @return 取消结果
     */
    @ApiOperation("取消设备租借")
    @PostMapping("/cancel-rental/{deviceId}")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> cancelRental(@PathVariable Long deviceId, Principal principal) {
        try {
            log.info("====== 收到设备取消租借请求 ======");
            log.info("设备ID: {}", deviceId);
            
            Long flyerId = Long.parseLong(principal.getName());
            log.info("飞手ID: {}", flyerId);
            
            // 首先检查设备是否属于当前飞手且处于租借状态
            boolean isDeviceAvailable = deviceService.checkDeviceAvailable(deviceId, flyerId);
            log.info("设备归属检查结果: {}", isDeviceAvailable);
            
            if (!isDeviceAvailable) {
                log.warn("设备不属于当前飞手或未处于租借状态");
                return Result.error("设备不属于当前飞手或未处于租借状态");
            }
            
            // 调用服务层执行取消租借操作
            log.info("开始调用deviceService.unbindDeviceFromFlyer");
            boolean success = deviceService.unbindDeviceFromFlyer(deviceId, null, true); // true表示取消租借
            log.info("deviceService.unbindDeviceFromFlyer返回结果: {}", success);
            
            if (success) {
                // 取消成功后，再次查询设备状态以验证
                DroneDevice updatedDevice = deviceService.getById(deviceId);
                if (updatedDevice != null) {
                    log.info("取消租借后设备状态验证: flyerId={}, rentalStatus={}", 
                            updatedDevice.getFlyerId(), updatedDevice.getRentalStatus());
                }
                log.info("设备取消租借成功，返回成功响应");
                return Result.ok("设备取消租借成功");
            } else {
                log.warn("设备取消租借失败");
                return Result.error("设备取消租借失败");
            }
        } catch (Exception e) {
            log.error("设备取消租借过程中发生异常: {}", e.getMessage(), e);
            return Result.error("设备取消租借过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 删除设备
     * @param deviceId 设备ID
     * @return 删除结果
     */
    @ApiOperation("删除设备")
    @DeleteMapping("/delete/{deviceId}")
    @PreAuthorize("hasRole('owner')")
    public Result<?> deleteDevice(@PathVariable Long deviceId) {
        try {
            // 获取当前用户ID作为ownerId
            Principal principal = SecurityContextHolder.getContext().getAuthentication();
            Long ownerId = Long.parseLong(principal.getName());

            // 检查设备是否存在
            DroneDevice existingDevice = deviceService.getById(deviceId);
            if (existingDevice == null) {
                return Result.error("设备不存在");
            }
            
            // 检查当前用户是否是设备所有者
            if (!existingDevice.getOwnerId().equals(ownerId)) {
                return Result.error("您没有权限删除此设备");
            }
            
            // 检查设备是否正在使用中
            if (existingDevice.getStatus() != DeviceStatusEnum.NORMAL.getCode() || existingDevice.getFlyerId() != null) {
                return Result.error("设备正在使用中，无法删除");
            }
            
            boolean success = deviceService.deleteDevice(deviceId);
            if (success) {
                log.info("设备删除成功，设备ID: {}, 所有者ID: {}", deviceId, ownerId);
                return Result.ok("设备删除成功");
            } else {
                log.error("设备删除失败，设备ID: {}", deviceId);
                return Result.error("设备删除失败");
            }
        } catch (NumberFormatException e) {
            log.error("用户认证信息异常: {}", e.getMessage());
            return Result.error("用户认证信息异常");
        } catch (Exception e) {
            log.error("删除设备时发生异常: {}", e.getMessage(), e);
            return Result.error("删除设备时发生异常: " + e.getMessage());
        }
    }

//    /**
//     * 归还无人机
//     * @param deviceId 设备ID
//     * @param principal 当前登录用户
//     * @return 归还结果
//     */
//    @ApiOperation("归还无人机")
//    @PostMapping("/return/{deviceId}")
//    @PreAuthorize("hasRole('flyer')")
//    public Result<?> returnDevice(@PathVariable Long deviceId, Principal principal) {
//        try {
//            log.info("====== 收到设备归还请求 ======");
//            log.info("设备ID: {}", deviceId);
//            
//            Long flyerId = Long.parseLong(principal.getName());
//            log.info("飞手ID: {}", flyerId);
//            
//            // 首先检查设备是否属于当前飞手且处于租借状态
//            boolean isDeviceAvailable = deviceService.checkDeviceAvailable(deviceId, flyerId);
//            log.info("设备归属检查结果: {}", isDeviceAvailable);
//            
//            if (!isDeviceAvailable) {
//                log.warn("设备不属于当前飞手或未处于租借状态");
//                return Result.error("设备不属于当前飞手或未处于租借状态");
//            }
//            
//            // 调用服务层执行归还操作
//            log.info("开始调用deviceService.unbindDeviceFromFlyer");
//            boolean success = deviceService.unbindDeviceFromFlyer(deviceId, null, false); // false表示归还操作
//            log.info("deviceService.unbindDeviceFromFlyer返回结果: {}", success);
//            
//            if (success) {
//                // 归还成功后，再次查询设备状态以验证
//                DroneDevice updatedDevice = deviceService.getById(deviceId);
//                if (updatedDevice != null) {
//                    log.info("归还后设备状态验证: flyerId={}, rentalStatus={}", 
//                            updatedDevice.getFlyerId(), updatedDevice.getRentalStatus());
//                }
//                log.info("设备归还成功，返回成功响应");
//                return Result.ok("设备归还成功");
//            } else {
//                log.warn("设备归还失败");
//                return Result.error("设备归还失败");
//            }
//        } catch (Exception e) {
//            log.error("设备归还过程中发生异常: {}", e.getMessage(), e);
//            return Result.error("设备归还过程中发生异常: " + e.getMessage());
//        }
//    }

    /**
     * 获取无人机设备详情
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @ApiOperation("获取设备详情")
    @GetMapping("/detail/{deviceId}")
    @PreAuthorize("hasRole('flyer') or hasRole('owner')")
    public Result<?> getDeviceDetail(@PathVariable Long deviceId) {
        DeviceDetailVO detail = deviceService.getDeviceDetail(deviceId);
        if (detail != null) {
            return Result.ok(detail);
        } else {
            return Result.error("设备不存在");
        }
    }

    /**
     * 编辑设备信息
     */
    @PostMapping("/edit/{deviceId}")
    @PreAuthorize("hasRole('owner')")
    public Result<?> editDevice(@PathVariable Long deviceId, @RequestBody DroneDevice device) {
        // 获取当前用户ID作为ownerId
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        Long ownerId = Long.parseLong(principal.getName());

        // 检查设备是否存在
        DroneDevice existingDevice = deviceService.getById(deviceId);
        if (existingDevice == null) {
            return Result.error("设备不存在");
        }
        if (!existingDevice.getOwnerId().equals(ownerId)) {
            return Result.error("您没有权限编辑此设备");
        }
        device.setDeviceId(deviceId);
        boolean success = deviceService.OwnerUpdateDeviceById(device);
        if (success) {
            return Result.ok("设备信息更新成功");
        }
        return Result.error("设备信息更新失败");
    }

    /**
     * 获取当前飞手的设备列表
     * @param principal 当前登录用户
     * @return 设备列表
     */
    @ApiOperation("获取当前飞手的设备列表")
    @GetMapping("/my-devices")
    @PreAuthorize("hasRole('flyer')")
    public Result<List<DroneDevice>> getMyDevices(Principal principal) {
        Long flyerId = Long.parseLong(principal.getName());
        List<DroneDevice> devices = deviceService.getDevicesByFlyerId(flyerId);
        return Result.ok(devices);
    }

    /**
     * 根据状态获取设备列表
     * @param status 设备状态编码
     * @return 设备列表
     */
    @ApiOperation("根据状态获取设备列表")
    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasRole('owner') or hasRole('admin')")
    public Result<?> getDevicesByStatus(@PathVariable Integer status) {
        DeviceStatusEnum statusEnum = DeviceStatusEnum.getByCode(status);
        if (statusEnum == null) {
            return Result.error("无效的设备状态");
        }
        List<DroneDevice> devices = deviceService.getDevicesByStatus(statusEnum);
        return Result.ok(devices);
    }
    
    /**
     * 获取所有设备列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 设备状态（可选）
     * @param principal 当前登录用户
     * @return 设备列表
     */
    @ApiOperation("获取所有设备列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('owner') or hasRole('admin')")
    public Result<?> getAllDevices(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            Principal principal) {
        log.info("开始处理获取设备列表请求，pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        
        try {
            // 获取当前用户ID作为ownerId
            if (principal == null) {
                log.error("Principal为null");
                return Result.error("用户未认证");
            }
            
            String principalName = principal.getName();
            log.info("Principal名称: {}", principalName);
            
            // 尝试直接解析用户ID
            Long ownerId;
            try {
                ownerId = Long.parseLong(principalName);
                log.info("直接解析用户ID成功: {}", ownerId);
                
                // 验证用户是否存在
                SysUser sysUser = sysUserMapper.selectById(ownerId);
                if (sysUser == null) {
                    log.error("未找到用户ID: {}", ownerId);
                    return Result.error("用户不存在");
                }
            } catch (NumberFormatException e) {
                log.error("Principal名称不是有效的用户ID: {}", principalName);
                return Result.error("用户认证信息异常");
            }
            
            // 调用服务层获取设备列表
            log.info("准备调用deviceService.getAllDevices");
            IPage<DeviceDetailVO> devices = deviceService.getAllDevices(pageNum, pageSize, ownerId, status);
            log.info("设备列表查询成功，总数: {}", devices.getTotal());
            
            // 添加详细日志以检查endurance字段是否被正确设置
            if (devices.getRecords() != null && !devices.getRecords().isEmpty()) {
                DeviceDetailVO firstDevice = devices.getRecords().get(0);
                if (firstDevice.getDeviceInfo() != null) {
                    log.info("第一台设备的endurance值: {}", firstDevice.getDeviceInfo().getEndurance());
                    log.info("设备信息字段完整列表: deviceName={}, model={}, maxLoad={}, endurance={}", 
                            firstDevice.getDeviceInfo().getDeviceName(),
                            firstDevice.getDeviceInfo().getModel(),
                            firstDevice.getDeviceInfo().getMaxLoad(),
                            firstDevice.getDeviceInfo().getEndurance());
                }
            }
            
            return Result.ok(devices);
        } catch (Exception e) {
            log.error("获取设备列表失败: {}", e.getMessage(), e);
            return Result.error("获取设备列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加设备
     * @param deviceInfo 设备信息
     * @param principal 当前登录用户（机主）
     * @return 添加结果
     */
    @ApiOperation("添加设备")
    @PostMapping("/add")
    @PreAuthorize("hasRole('owner')")
    public Result<?> addDevice(@RequestBody Map<String, Object> deviceInfo, Principal principal) {
        try {
            // 获取当前登录的机主ID
            Long ownerId = Long.parseLong(principal.getName());
            
            // 创建设备实体
            DroneDevice device = new DroneDevice();
            
            // 从请求体中获取字段并设置到设备实体中，确保匹配数据库表结构
            device.setOwnerId(ownerId);
            // 设置设备名称
            if (deviceInfo.containsKey("deviceName")) {
                device.setDeviceName((String) deviceInfo.get("deviceName"));
            }
            device.setModel((String) deviceInfo.get("deviceModel"));
            // 使用序列号作为设备序列号（必填且唯一）
            device.setSerialNumber((String) deviceInfo.get("serialNumber"));
            
            // 设置制造商/品牌
            if (deviceInfo.containsKey("manufacturer")) {
                device.setManufacturer((String) deviceInfo.get("manufacturer"));
            }
            
            // 设置购买时间（使用LocalDateTime类型）
            String purchaseDateStr = (String) deviceInfo.get("purchaseDate");
            if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
                try {
                    // 直接使用LocalDateTime解析日期
                    LocalDateTime purchaseTime = LocalDateTime.parse(purchaseDateStr + "T00:00:00");
                    device.setPurchaseTime(purchaseTime);
                } catch (Exception e) {
                    // 日期格式错误时记录日志但继续执行
                    log.error("日期格式解析错误: {}", e.getMessage());
                }
            }
            // 设置最大载重
            if (deviceInfo.containsKey("maxLoad")) {
                try {
                    device.setMaxLoad(Double.parseDouble(deviceInfo.get("maxLoad").toString()));
                } catch (Exception e) {
                    log.error("最大载重格式解析错误: {}", e.getMessage());
                }
            }
            
            // 设置品牌（映射到deviceType字段）
            if (deviceInfo.containsKey("brand")) {
                device.setDeviceType((String) deviceInfo.get("brand"));
            }
            
            // 设置续航时间
            if (deviceInfo.containsKey("endurance")) {
                try {
                    device.setEndurance(String.valueOf(Integer.parseInt(deviceInfo.get("endurance").toString())));
                } catch (Exception e) {
                    log.error("续航时间格式解析错误: {}", e.getMessage());
                }
            }
            
            // 设置设备状态
            String statusStr = (String) deviceInfo.get("status");
            if (statusStr != null) {
                try {
                    device.setStatus(Integer.parseInt(statusStr));
                } catch (NumberFormatException e) {
                    // 状态解析错误时使用默认值
                    device.setStatus(1); // 默认正常状态
                }
            } else {
                device.setStatus(1); // 默认正常状态
            }
            
            // 设置设备图片
            if (deviceInfo.containsKey("picture")) {
                device.setPicture((String) deviceInfo.get("picture"));
            }
            
            // 注意：移除了对deviceType和isQualified字段的设置，因为数据库表中不存在这些字段
            
            // 保存设备
            deviceService.save(device);
            
            return Result.ok("设备添加成功");
        } catch (Exception e) {
            return Result.error("设备添加失败：" + e.getMessage());
        }
    }
    
    /**
     * 归还设备（与取消租借功能相同）
     * @param deviceId 设备ID
     * @param principal 当前登录用户
     * @return 归还结果
     */
    @ApiOperation("归还设备")
    @PostMapping("/return/{deviceId}")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> returnDeviceByFlyer(@PathVariable Long deviceId, Principal principal) {
        try {
            log.info("====== 收到设备归还请求 ======");
            log.info("设备ID: {}", deviceId);
            
            Long flyerId = Long.parseLong(principal.getName());
            log.info("飞手ID: {}", flyerId);
            
            // 首先检查设备是否属于当前飞手且处于租借状态
            boolean isDeviceAvailable = deviceService.checkDeviceAvailable(deviceId, flyerId);
            log.info("设备归属检查结果: {}", isDeviceAvailable);
            
            if (!isDeviceAvailable) {
                log.warn("设备不属于当前飞手或未处于租借状态");
                return Result.error("设备不属于当前飞手或未处于租借状态");
            }
            
            // 调用服务层执行归还操作
            log.info("开始调用deviceService.unbindDeviceFromFlyer");
            boolean success = deviceService.unbindDeviceFromFlyer(deviceId, false); // false表示归还设备
            log.info("deviceService.unbindDeviceFromFlyer返回结果: {}", success);
            
            if (success) {
                // 归还成功后，再次查询设备状态以验证
                DroneDevice updatedDevice = deviceService.getById(deviceId);
                if (updatedDevice != null) {
                    log.info("归还后设备状态验证: flyerId={}, rentalStatus={}", 
                            updatedDevice.getFlyerId(), updatedDevice.getRentalStatus());
                }
                log.info("设备归还成功，返回成功响应");
                return Result.ok("设备归还成功");
            } else {
                log.warn("设备归还失败");
                return Result.error("设备归还失败");
            }
        } catch (Exception e) {
            log.error("设备归还过程中发生异常: {}", e.getMessage(), e);
            return Result.error("设备归还过程中发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 获取飞手租借历史记录
     * @param principal 当前登录用户
     * @return 租借历史记录列表
     */
    @ApiOperation("获取飞手租借历史记录")
    @GetMapping("/rental/history")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> getFlyerRentalHistory(Principal principal) {
        try {
            Long flyerId = Long.parseLong(principal.getName());
            log.info("获取飞手租借历史记录，飞手ID: {}", flyerId);
            
            // 调用服务层获取租借历史记录
            List<Map<String, Object>> rentalHistory = deviceService.getFlyerRentalHistory(flyerId);
            return Result.ok(rentalHistory);
        } catch (Exception e) {
            log.error("获取租借历史记录失败: {}", e.getMessage(), e);
            return Result.error("获取租借历史记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取飞手当前租借的设备
     * @param principal 当前登录用户
     * @return 当前租借设备列表
     */
    @ApiOperation("获取飞手当前租借的设备")
    @GetMapping("/rental/current")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> getFlyerCurrentRentals(Principal principal) {
        try {
            Long flyerId = Long.parseLong(principal.getName());
            log.info("获取飞手当前租借设备，飞手ID: {}", flyerId);
            
            // 获取当前租借的设备
            List<DroneDevice> currentRentals = deviceService.getDevicesByFlyerId(flyerId);
            return Result.ok(currentRentals);
        } catch (Exception e) {
            log.error("获取当前租借设备失败: {}", e.getMessage(), e);
            return Result.error("获取当前租借设备失败: " + e.getMessage());
        }
    }
}