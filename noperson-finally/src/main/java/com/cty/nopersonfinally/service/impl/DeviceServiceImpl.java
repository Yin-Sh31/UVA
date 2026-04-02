package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.DeviceMapper;
import com.cty.nopersonfinally.mapper.DeviceOperateLogMapper;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import com.cty.nopersonfinally.mapper.UserOwnerMapper;
import com.cty.nopersonfinally.pojo.entity.DeviceOperateLog;
import com.cty.nopersonfinally.pojo.entity.DeviceRental;
import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.entity.UserOwner;
import com.cty.nopersonfinally.pojo.enums.DeviceStatusEnum;
import com.cty.nopersonfinally.pojo.vo.DeviceDetailVO;
import com.cty.nopersonfinally.service.DeviceMaintainService;
import com.cty.nopersonfinally.service.DeviceRentalService;
import com.cty.nopersonfinally.service.DeviceService;
import com.cty.nopersonfinally.utils.BusinessException;
import jakarta.annotation.Resource;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 设备管理服务实现类
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, DroneDevice> implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private DeviceOperateLogMapper deviceOperateLogMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private DeviceMaintainService deviceMaintainService;
    
    @Resource
    private SysUserMapper sysUserMapper;
    
    @Resource
    private UserOwnerMapper userOwnerMapper;
    
    @Resource
    private DeviceRentalService deviceRentalService;

    private static final String DEVICE_STATUS_CACHE_KEY = "device:status:";
    private static final String FLYER_DEVICES_CACHE_KEY = "flyer:devices:";

    @Override
    @Transactional
    public boolean updateDeviceStatus(Long deviceId, DeviceStatusEnum status, Long operatorId) {
        // 1. 校验设备是否存在
        DroneDevice device = getById(deviceId);
        if (device == null) {
            return false;
        }

        // 2. 状态未变更则直接返回
        if (device.getStatus() == status.getCode()) {
            return true;
        }

        // 3. 更新设备状态
        DroneDevice updateEntity = new DroneDevice();
        updateEntity.setDeviceId(deviceId);
        updateEntity.setStatus(status.getCode());
        updateEntity.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(updateEntity);
        if (result) {
            // 4. 记录操作日志
            DeviceOperateLog operateLog = new DeviceOperateLog();
            operateLog.setDeviceId(deviceId);
            operateLog.setOperatorId(operatorId);
            operateLog.setBeforeStatus(device.getStatus());
            operateLog.setAfterStatus(status.getCode());
            operateLog.setOperateTime(new Date());
            deviceOperateLogMapper.insert(operateLog);

            // 5. 更新缓存
            redisTemplate.opsForValue().set(DEVICE_STATUS_CACHE_KEY + deviceId, status.getCode(), 24, TimeUnit.HOURS);
            // 清除飞手设备列表缓存
            redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + device.getFlyerId());
        }

        return result;
    }

    @Override
    public List<DroneDevice> getDevicesByFlyerId(Long flyerId) {
        // 尝试从缓存获取
        String cacheKey = FLYER_DEVICES_CACHE_KEY + flyerId;
        List<DroneDevice> devices = (List<DroneDevice>) redisTemplate.opsForValue().get(cacheKey);

        if (devices == null) {
            // 缓存未命中，从数据库查询
            QueryWrapper<DroneDevice> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("flyer_id", flyerId)
                    .eq("is_deleted", 0)
                    .orderByDesc("create_time");
            devices = deviceMapper.selectList(queryWrapper);

            // 存入缓存，有效期1小时
            redisTemplate.opsForValue().set(cacheKey, devices, 1, TimeUnit.HOURS);
        }

        return devices;
    }

    @Override
    public List<DroneDevice> getDevicesByStatus(DeviceStatusEnum status) {
        QueryWrapper<DroneDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status.getCode())
                .eq("is_deleted", 0);
        
        // 只对特定状态查询合格设备
        if (status == DeviceStatusEnum.NORMAL) {
            queryWrapper.eq("is_qualified", 1);
        }
        
        // 按维护时间排序，同时保留flyerId信息以显示租借状态
        queryWrapper.orderByAsc("last_maintain_time");

        return deviceMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<DroneDevice> getAvailableDevices() {
        QueryWrapper<DroneDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", DeviceStatusEnum.NORMAL.getCode())
                .eq("is_deleted", 0)
                .isNull("flyer_id") // 关键条件：未被租借
                .eq("rental_status", 0); // 添加rental_status=0的条件
        queryWrapper.orderByAsc("last_maintain_time");
        
        return deviceMapper.selectList(queryWrapper);
    }

    @Override
    public boolean checkDeviceAvailable(Long deviceId) {
        // 缓存机制不太适合检查设备是否被租借，因为需要检查flyerId字段
        // 直接查询数据库获取完整信息
        DroneDevice device = getById(deviceId);
        if (device == null) {
            System.out.println("checkDeviceAvailable失败：设备ID " + deviceId + " 不存在");
            return false;
        }
        
        System.out.println("checkDeviceAvailable设备ID " + deviceId + " 信息：status=" + device.getStatus() + 
                         ", isDeleted=" + device.getIsDeleted() +
                         ", expireTime=" + device.getExpireTime() +
                         ", rental_status=" + device.getRentalStatus() +
                         ", flyerId=" + device.getFlyerId());

        // 设备需同时满足：正常状态、未删除、未被租借(flyerId为null)、可租借状态
        // 移除isQualified检查，因为该字段在数据库中不存在(@TableField(exist = false))
        // 移除expireTime检查，因为从日志看该字段也为null
        boolean statusCheck = device.getStatus() == DeviceStatusEnum.NORMAL.getCode();
        boolean deletedCheck = device.getIsDeleted() == 0;
        boolean flyerCheck = device.getFlyerId() == null;
        // 允许rental_status为null或等于0的情况都视为可租借状态
        boolean rentalCheck =  device.getRentalStatus() == 0;
        
        System.out.println("checkDeviceAvailable检查结果：statusCheck=" + statusCheck + 
                         ", deletedCheck=" + deletedCheck +
                         ", flyerCheck=" + flyerCheck +
                         ", rentalCheck=" + rentalCheck);
        
        boolean available = statusCheck && deletedCheck && flyerCheck && rentalCheck;
        
        if (!available) {
            System.out.println("checkDeviceAvailable：设备ID " + deviceId + " 不可用");
        }

        return available;
    }

    @Override
    @Transactional
    public int bindDeviceToFlyer(Long deviceId, Long flyerId) {
        DroneDevice device = getById(deviceId);
        if (device == null) {
            return 0;
        }

        if (device.getFlyerId() != null) {
            return 0; // 已绑定同一飞手，无需重复操作
        }

        DroneDevice updateEntity = new DroneDevice();
        updateEntity.setDeviceId(deviceId);
        updateEntity.setFlyerId(flyerId);
        updateEntity.setRentalStatus(1); // 绑定设备时更新租借状态为已租借(1)
        updateEntity.setUpdateTime(LocalDateTime.now());

        boolean result = updateById(updateEntity);
        if (result) {
            // 清除相关缓存
            redisTemplate.delete(DEVICE_STATUS_CACHE_KEY + deviceId);
            if (device.getFlyerId() != null) {
                redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + device.getFlyerId());
            }
            redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + flyerId);
            return 1;
        }

        return 0;
    }


    /**
     * 解除设备与飞手的绑定
     * @param deviceId 设备ID
     * @param isCancel 是否为取消租借操作，true表示取消租借(状态3)，false表示归还操作(状态2)
     * @return 是否解绑成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindDeviceFromFlyer(Long deviceId, boolean isCancel) {
        // 调用带rentalId的重载方法，rentalId为null时会使用最新记录
        return unbindDeviceFromFlyer(deviceId, null, isCancel);
    }
    
    /**
     * 解除设备与飞手的绑定（支持指定rentalId）
     * @param deviceId 设备ID
     * @param rentalId 租借记录ID，指定要更新的特定租借记录
     * @param isCancel 是否为取消租借操作，true表示取消租借(状态3)，false表示归还操作(状态2)
     * @return 是否解绑成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindDeviceFromFlyer(Long deviceId, Long rentalId, boolean isCancel) {
        try {
            System.out.println("====== 开始执行设备解绑操作 ======");
            System.out.println("设备ID=" + deviceId);
            if (rentalId != null) {
                System.out.println("指定租借记录ID=" + rentalId);
            }
            System.out.println("操作类型：" + (isCancel ? "取消租借" : "归还设备"));
            
            // 1. 如果指定了rentalId，直接以rentalId为准进行操作
            if (rentalId != null) {
                System.out.println("1. 根据指定的rentalId进行操作...");
                // 查询指定的租借记录
                DeviceRental rentalRecord = deviceRentalService.getById(rentalId);
                if (rentalRecord == null) {
                    System.out.println("未找到指定ID的租借记录：" + rentalId);
                    return false;
                }
                
                System.out.println("找到租借记录：rentalId=" + rentalRecord.getRentalId() + ", deviceId=" + rentalRecord.getDeviceId() + ", flyerId=" + rentalRecord.getFlyerId());
                
                // 验证该租借记录是否与传入的deviceId匹配
                if (!rentalRecord.getDeviceId().equals(deviceId)) {
                    System.out.println("错误：租借记录的设备ID与传入的设备ID不匹配");
                    return false;
                }
                
                Long oldFlyerId = rentalRecord.getFlyerId();
                
                // 更新租借记录状态
                int rentalStatus = isCancel ? 3 : 2; // 3-已取消，2-已归还
                System.out.println("更新租借记录状态为：" + rentalStatus + "(" + (isCancel ? "已取消" : "已归还") + ")");
                
                DeviceRental updateRental = new DeviceRental();
                updateRental.setRentalId(rentalRecord.getRentalId());
                updateRental.setRentalStatus(rentalStatus);
                updateRental.setRentalEndTime(new Date());
                updateRental.setUpdateTime(new Date());
                
                boolean rentalUpdateResult = deviceRentalService.updateById(updateRental);
                System.out.println("租借记录更新结果：" + rentalUpdateResult);
                
                if (!rentalUpdateResult) {
                    System.out.println("租借记录更新失败");
                    throw new RuntimeException("租借记录更新失败");
                }
                
                // 更新设备信息，解除绑定
                System.out.println("2. 解除设备与飞手的绑定...");
                UpdateWrapper<DroneDevice> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("device_id", deviceId)
                             .eq("flyer_id", oldFlyerId) // 确保只更新当前绑定的飞手的数据
                             .eq("is_deleted", 0); // 确保设备未删除
                
                updateWrapper.set("flyer_id", null);
                updateWrapper.set("rental_status", 0);
                updateWrapper.set("update_time", LocalDateTime.now());
                
                int updateCount = deviceMapper.update(null, updateWrapper);
                System.out.println("设备更新结果：影响行数=" + updateCount);
                
                if (updateCount <= 0) {
                    System.out.println("设备状态更新失败，可能被其他事务修改");
                    throw new RuntimeException("设备状态更新失败，可能被其他事务修改");
                }
                
                // 清除缓存
                System.out.println("3. 清除相关缓存...");
                redisTemplate.delete(DEVICE_STATUS_CACHE_KEY + deviceId);
                redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + oldFlyerId);
                
                System.out.println("====== 设备解绑操作完成 ======");
                System.out.println("设备ID=" + deviceId + " 已成功解除绑定，原飞手ID=" + oldFlyerId);
                return true;
            }
            
            // 2. 如果未指定rentalId，使用原始逻辑（基于deviceId和flyerId查询最新记录）
            System.out.println("1. 检查设备是否存在...");
            DroneDevice device = getById(deviceId);
            if (device == null) {
                System.out.println("解绑失败：设备ID " + deviceId + " 不存在");
                return false;
            }
            
            System.out.println("设备查询结果：deviceId=" + device.getDeviceId() + 
                            ", status=" + device.getStatus() + 
                            ", isDeleted=" + device.getIsDeleted() +
                            ", rental_status=" + device.getRentalStatus() +
                            ", flyerId=" + device.getFlyerId() +
                            ", ownerId=" + device.getOwnerId());
            
            // 检查设备是否已绑定飞手
            if (device.getFlyerId() == null) {
                System.out.println("设备未绑定飞手，无需解绑操作");
                return true;
            }
            
            Long oldFlyerId = device.getFlyerId();
            
            // 查询最新的租借记录
            System.out.println("2. 查询并更新租借记录...");
            QueryWrapper<DeviceRental> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("device_id", deviceId)
                       .eq("flyer_id", oldFlyerId)
                       .orderByDesc("rental_start_time")
                       .last("LIMIT 1");
            
            DeviceRental activeRental = deviceRentalService.getOne(queryWrapper);
            
            if (activeRental != null) {
                System.out.println("找到当前设备和飞手的最新租借记录：rentalId=" + activeRental.getRentalId());
                
                // 更新租借记录状态
                int rentalStatus = isCancel ? 3 : 2; // 3-已取消，2-已归还
                System.out.println("更新租借记录状态为：" + rentalStatus + "(" + (isCancel ? "已取消" : "已归还") + ")");
                
                DeviceRental updateRental = new DeviceRental();
                updateRental.setRentalId(activeRental.getRentalId());
                updateRental.setRentalStatus(rentalStatus);
                updateRental.setRentalEndTime(new Date());
                updateRental.setUpdateTime(new Date());
                
                boolean rentalUpdateResult = deviceRentalService.updateById(updateRental);
                System.out.println("租借记录更新结果：" + rentalUpdateResult);
                
                if (!rentalUpdateResult) {
                    System.out.println("租借记录更新失败");
                    throw new RuntimeException("租借记录更新失败");
                }
            }
            
            // 更新设备信息，解除绑定
            System.out.println("3. 执行数据库更新，解除飞手绑定...");
            UpdateWrapper<DroneDevice> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("device_id", deviceId)
                         .eq("flyer_id", oldFlyerId) // 确保只更新当前绑定的飞手的数据
                         .eq("is_deleted", 0); // 确保设备未删除
            
            updateWrapper.set("flyer_id", null);
            updateWrapper.set("rental_status", 0);
            updateWrapper.set("update_time", LocalDateTime.now());
            
            int updateCount = deviceMapper.update(null, updateWrapper);
            System.out.println("设备更新结果：影响行数=" + updateCount);
            
            if (updateCount <= 0) {
                System.out.println("解绑失败：设备状态更新失败，可能被其他事务修改");
                throw new RuntimeException("设备状态更新失败，可能被其他事务修改");
            }
            
            // 清除缓存
            System.out.println("4. 清除相关缓存...");
            redisTemplate.delete(DEVICE_STATUS_CACHE_KEY + deviceId);
            redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + oldFlyerId);
            
            System.out.println("====== 设备解绑操作完成 ======");
            System.out.println("设备ID=" + deviceId + " 已成功解除绑定，原飞手ID=" + oldFlyerId);
            return true;
        } catch (Exception e) {
            System.out.println("解绑操作发生异常：" + e.getMessage());
            e.printStackTrace();
            // 重新抛出异常以确保事务回滚
            throw e;
        }
    }
    
    // 保留原有方法签名以保持兼容性
    public boolean unbindDeviceFromFlyer(Long deviceId) {
        // 默认作为归还操作处理
        return unbindDeviceFromFlyer(deviceId, false);
    }


    @Override
    public boolean checkDeviceAvailable(Long deviceId, Long flyerId) {
        // 1. 先检查设备是否存在且属于该飞手
        DroneDevice device = getById(deviceId);
        if (device == null || !device.getFlyerId().equals(flyerId)) {
            return false;
        }
        // 2. 检查设备是否正常可用（状态正常、未删除、已被租借状态）
        // 移除isQualified和expireTime检查，因为这些字段在数据库中不存在(@TableField(exist = false))
        return device.getStatus() == DeviceStatusEnum.NORMAL.getCode()
                && device.getIsDeleted() == 0
                && device.getRentalStatus() != null && device.getRentalStatus() == 1; // 已被租借状态
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rentDevice(Long deviceId, Long flyerId) {
        try {
            System.out.println("====== 开始执行设备租借操作 ======");
            System.out.println("设备ID=" + deviceId + ", 飞手ID=" + flyerId);
            
            // 1. 检查设备是否存在
            System.out.println("1. 查询设备信息...");
            DroneDevice device = getById(deviceId);
            if (device == null) {
                System.out.println("租借失败：设备ID " + deviceId + " 不存在");
                return false;
            }
            
            System.out.println("设备查询结果：deviceId=" + device.getDeviceId() + 
                            ", status=" + device.getStatus() + 
                            ", isDeleted=" + device.getIsDeleted() +
                            ", rental_status=" + device.getRentalStatus() +
                            ", flyerId=" + device.getFlyerId() +
                            ", ownerId=" + device.getOwnerId());
            
            // 2. 全面检查设备可用性
            System.out.println("2. 检查设备可用性...");
            // 检查设备是否正常状态
            if (device.getStatus() != DeviceStatusEnum.NORMAL.getCode()) {
                System.out.println("租借失败：设备状态不是NORMAL，当前状态：" + device.getStatus());
                return false;
            }
            // 检查设备是否已删除
            if (device.getIsDeleted() != 0) {
                System.out.println("租借失败：设备已删除");
                return false;
            }
            // 检查设备是否已被租借
            if (device.getFlyerId() != null) {
                System.out.println("租借失败：设备已被租借，关联的 flyerId=" + device.getFlyerId());
                return false;
            }

            // 检查设备租借状态
            if(device.getRentalStatus() != 0){
                System.out.println("租借失败：设备不可租借，rental_status=" + device.getRentalStatus());
                return false;
            }

            // 3. 记录是否有历史租借记录（不再阻止重复租借）
            System.out.println("3. 检查是否存在历史租借记录...");
            DeviceRental existingRental = deviceRentalService.getActiveRental(deviceId, flyerId);
            if (existingRental != null && existingRental.getRentalStatus() == 1) {
                System.out.println("发现历史有效租借记录，但允许重复租借，继续执行租借操作，rentalId=" + existingRental.getRentalId());
            }
            
            // 4. 更新设备信息，使用乐观锁防止并发问题
            System.out.println("4. 更新设备信息，绑定飞手并设置租借状态...");
            DroneDevice updateEntity = new DroneDevice();
            updateEntity.setDeviceId(deviceId);
            updateEntity.setFlyerId(flyerId);
            updateEntity.setRentalStatus(1); // 设置为已租借状态
            updateEntity.setUpdateTime(LocalDateTime.now());
            
            // 添加更新条件，确保只在设备未被其他事务修改的情况下更新
            QueryWrapper<DroneDevice> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("device_id", deviceId)
                         .eq("rental_status", 0)  // 确保状态仍为可租借
                         .isNull("flyer_id")      // 确保没有被其他飞手租借
                         .eq("is_deleted", 0);    // 确保设备未删除
            
            System.out.println("准备执行更新：updateEntity=" + updateEntity.toString());
            boolean updateResult = deviceMapper.update(updateEntity, updateWrapper) > 0;
            System.out.println("设备更新结果：" + updateResult);
            
            if (!updateResult) {
                System.out.println("租借失败：设备状态更新失败，可能被其他事务修改");
                // 明确抛出异常触发事务回滚
                throw new RuntimeException("设备状态更新失败，可能被其他事务修改");
            }
            
            // 验证更新是否成功
            System.out.println("验证设备更新是否成功...");
            DroneDevice updatedDevice = getById(deviceId);
            if (updatedDevice != null) {
                System.out.println("更新后的设备信息：flyerId=" + updatedDevice.getFlyerId() + 
                                ", rental_status=" + updatedDevice.getRentalStatus());
                if (updatedDevice.getFlyerId() == null || updatedDevice.getFlyerId().longValue() != flyerId.longValue() ||
                    updatedDevice.getRentalStatus() == null || updatedDevice.getRentalStatus() != 1) {
                    System.out.println("警告：设备更新后的值与预期不符！");
                    // 如果更新后的值与预期不符，抛出异常回滚事务
                    throw new RuntimeException("设备状态更新后的值与预期不符");
                }
            }
            
            // 5. 创建租借记录
            System.out.println("5. 创建设备租借记录...");
            DeviceRental rentalRecord = deviceRentalService.createRentalRecord(deviceId, flyerId, device.getOwnerId());
            System.out.println("创建租借记录结果：" + (rentalRecord != null ? "成功，rentalId=" + rentalRecord.getRentalId() : "失败"));
            
            if (rentalRecord == null) {
                System.out.println("租借失败：创建租借记录失败");
                throw new RuntimeException("创建租借记录失败");
            }
            
            // 6. 清除相关缓存
            System.out.println("6. 清除相关缓存...");
            redisTemplate.delete(DEVICE_STATUS_CACHE_KEY + deviceId);
            redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + flyerId);
            System.out.println("缓存清除完成");
            
            System.out.println("====== 设备租借操作完成 ======");
            System.out.println("设备ID=" + deviceId + " 已成功绑定到飞手ID=" + flyerId);
            return true;
        } catch (Exception e) {
            System.out.println("租借操作发生异常：" + e.getMessage());
            e.printStackTrace();
            // 重新抛出异常以确保事务回滚
            throw e;
        }
    }
    
    @Override
    @Transactional
    public boolean returnDevice(Long deviceId, Long flyerId) {
        System.out.println("归还设备：deviceId=" + deviceId + ", flyerId=" + flyerId);
        
        // 1. 检查设备是否存在
        DroneDevice device = getById(deviceId);
        if (device == null) {
            System.out.println("归还失败：设备ID " + deviceId + " 不存在");
            return false;
        }
        
        // 2. 检查设备是否属于当前飞手（添加null检查）
        if (device.getFlyerId() == null || !device.getFlyerId().equals(flyerId)) {
            System.out.println("归还失败：设备不属于当前飞手，device.flyerId=" + device.getFlyerId() + ", request.flyerId=" + flyerId);
            return false;
        }
        
        // 3. 检查设备状态
        if (device.getStatus() != DeviceStatusEnum.NORMAL.getCode()) {
            System.out.println("归还失败：设备状态异常，status=" + device.getStatus());
            return false;
        }
        
        // 4. 更新设备租借状态为可租借(0)并解除飞手绑定
        DroneDevice updateEntity = new DroneDevice();
        updateEntity.setDeviceId(deviceId);
        updateEntity.setRentalStatus(0); // 明确设置为0，表示可租借
        updateEntity.setFlyerId(null);
        updateEntity.setUpdateTime(LocalDateTime.now());
        boolean updateResult = updateById(updateEntity);
        System.out.println("更新设备租借状态和解除绑定结果：" + updateResult);
        
        // 5. 清除相关缓存
        redisTemplate.delete(DEVICE_STATUS_CACHE_KEY + deviceId);
        redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + flyerId);
        
        return updateResult;
    }
    
    @Override
    public DeviceDetailVO getDeviceDetail(Long deviceId) {
        // 1. 查询设备基本信息
        DroneDevice device = getById(deviceId);
        if (device == null) {
            return null;
        }
        
        DeviceDetailVO vo = new DeviceDetailVO();
        
        // 2. 设置设备基本信息
        DeviceDetailVO.DroneDeviceBasicInfo basicInfo = new DeviceDetailVO.DroneDeviceBasicInfo();
        basicInfo.setDeviceId(device.getDeviceId());
        basicInfo.setOwnerId(device.getOwnerId());
        basicInfo.setFlyerId(device.getFlyerId());
        basicInfo.setModel(device.getModel());
        basicInfo.setDeviceName(device.getDeviceName());
        basicInfo.setEndurance(device.getEndurance());
        basicInfo.setSerialNumber(device.getSerialNumber());
        basicInfo.setDeviceType(device.getDeviceType());
        basicInfo.setStatus(device.getStatus());
        basicInfo.setStatusDesc(DeviceStatusEnum.getByCode(device.getStatus()).getDesc());
        basicInfo.setManufacturer(device.getManufacturer());
        // 添加设备图片
        basicInfo.setPicture(device.getPicture());
        // 设置设备租借状态（添加null检查，默认值为0）
        basicInfo.setRental_status(device.getRentalStatus() != null ? device.getRentalStatus() : 0);
        
        // 格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (device.getPurchaseTime() != null) {
            basicInfo.setPurchaseTime(device.getPurchaseTime().format(formatter));
        }
        if (device.getLastMaintainTime() != null) {
            basicInfo.setLastMaintainTime(device.getLastMaintainTime().format(formatter));
        }
        
        // 确保isQualified有值，避免前端出现空值问题
        basicInfo.setIsQualified(device.getIsQualified() != null ? device.getIsQualified() : 0);
        basicInfo.setMaxLoad(device.getMaxLoad());
        
        vo.setDeviceInfo(basicInfo);
        
        // 3. 查询最近的维护记录
        vo.setLastMaintainRecord(deviceMaintainService.getLastMaintainRecord(deviceId));
        
        // 4. 查询维护记录列表（最近5条）
        vo.setMaintainRecords(deviceMaintainService.getDeviceMaintainRecords(deviceId, 1, 5).getRecords());
        
        // 5. 查询机主信息
        if (device.getOwnerId() != null) {
            DeviceDetailVO.OwnerInfo ownerInfo = new DeviceDetailVO.OwnerInfo();
            
            // 通过owner_id查询sys_user表获取机主信息
            SysUser sysUser = sysUserMapper.selectById(device.getOwnerId());
            if (sysUser != null) {
                ownerInfo.setRealName(sysUser.getRealName());
                ownerInfo.setPhone(sysUser.getPhone());
            }
            
            vo.setOwnerInfo(ownerInfo);
        }
        
        return vo;
    }

    @Override
    public IPage<DeviceDetailVO> getAllDevices(int pageNum, int pageSize, Long ownerId, Integer status) {
        // 创建分页对象
        Page<DroneDevice> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<DroneDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        // 添加机主ID筛选条件
        if (ownerId != null) {
            queryWrapper.eq("owner_id", ownerId);
        }
        
        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        IPage<DroneDevice> devicePage = page(page, queryWrapper);
        
        // 转换为DeviceDetailVO
        IPage<DeviceDetailVO> resultPage = devicePage.convert(device -> {
            // 构建DeviceDetailVO对象
            DeviceDetailVO detailVO = new DeviceDetailVO();
            
            // 设置设备基本信息
            DeviceDetailVO.DroneDeviceBasicInfo basicInfo = new DeviceDetailVO.DroneDeviceBasicInfo();
            basicInfo.setDeviceId(device.getDeviceId());
            basicInfo.setOwnerId(device.getOwnerId());
            basicInfo.setFlyerId(device.getFlyerId());
            basicInfo.setDeviceName(device.getDeviceName());
            basicInfo.setModel(device.getModel());
            basicInfo.setEndurance(device.getEndurance());
            basicInfo.setSerialNumber(device.getSerialNumber());
            basicInfo.setDeviceType(device.getDeviceType());
            basicInfo.setStatus(device.getStatus());
            basicInfo.setStatusDesc(DeviceStatusEnum.getByCode(device.getStatus()).getDesc());
            basicInfo.setManufacturer(device.getManufacturer());
            // 添加设备图片
            basicInfo.setPicture(device.getPicture());
            
            // 格式化时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (device.getPurchaseTime() != null) {
                basicInfo.setPurchaseTime(device.getPurchaseTime().format(formatter));
            }
            if (device.getLastMaintainTime() != null) {
                basicInfo.setLastMaintainTime(device.getLastMaintainTime().format(formatter));
            }
            
            // 确保isQualified有值，避免前端出现空值问题
            basicInfo.setIsQualified(device.getIsQualified() != null ? device.getIsQualified() : 0);
            basicInfo.setMaxLoad(device.getMaxLoad());
            
            detailVO.setDeviceInfo(basicInfo);
                
            // 查询机主信息
            if (device.getOwnerId() != null) {
                DeviceDetailVO.OwnerInfo ownerInfo = new DeviceDetailVO.OwnerInfo();
                
                // 通过owner_id查询user_owner表获取机主信息
                UserOwner userOwner = userOwnerMapper.selectOne(new QueryWrapper<UserOwner>()
                        .eq("user_id", device.getOwnerId())
                        .eq("is_deleted", 0));
                
                if (userOwner != null) {
                    // 设置机主姓名
                    ownerInfo.setRealName(userOwner.getRealName());
                    
                    // 查询sys_user表获取机主电话
                    SysUser sysUser = sysUserMapper.selectById(device.getOwnerId());
                    if (sysUser != null) {
                        ownerInfo.setPhone(sysUser.getPhone());
                    }
                }
                
                detailVO.setOwnerInfo(ownerInfo);
            }
                
            // 查询最近的维护记录
            detailVO.setLastMaintainRecord(deviceMaintainService.getLastMaintainRecord(device.getDeviceId()));
            
            // 查询维护记录列表（最近5条）
            detailVO.setMaintainRecords(deviceMaintainService.getDeviceMaintainRecords(device.getDeviceId(), 1, 5).getRecords());
            
            return detailVO;
        });
        
        return resultPage;
    }
    
    @Override
    @Transactional
    public boolean auditDeviceQualification(Long deviceId, Integer isQualified, Long operatorId) {
        // 1. 校验设备是否存在
        DroneDevice device = getById(deviceId);
        if (device == null) {
            return false;
        }
        
        // 2. 校验isQualified参数
        if (isQualified != 0 && isQualified != 1) {
            return false;
        }
        
        // 3. 更新设备资质状态
        DroneDevice updateEntity = new DroneDevice();
        updateEntity.setDeviceId(deviceId);
        updateEntity.setIsQualified(isQualified);
        updateEntity.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(updateEntity);
        if (result) {
            // 4. 记录操作日志
            DeviceOperateLog operateLog = new DeviceOperateLog();
            operateLog.setDeviceId(deviceId);
            operateLog.setOperatorId(operatorId);
            operateLog.setBeforeStatus(device.getIsQualified());
            operateLog.setAfterStatus(isQualified);
            operateLog.setOperateTime(new Date());
            deviceOperateLogMapper.insert(operateLog);
            
            // 5. 清除相关缓存
            redisTemplate.delete(DEVICE_STATUS_CACHE_KEY + deviceId);
            if (device.getFlyerId() != null) {
                redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + device.getFlyerId());
            }
        }
        
        return result;
    }
    
    @Override
    public IPage<DroneDevice> getPendingAuditDevices(int pageNum, int pageSize) {
        // 创建分页对象
        Page<DroneDevice> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件 - 查询待审核的设备（is_qualified = 2 表示待审核）
        QueryWrapper<DroneDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_qualified", 2)
                .eq("is_deleted", 0)
                .orderByDesc("create_time");
        
        // 执行分页查询
        return page(page, queryWrapper);
    }
    
    @Override
    public IPage<DeviceDetailVO> getDevicesForAudit(int pageNum, int pageSize, Long ownerId) {
        // 创建分页对象
        Page<DroneDevice> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件 - 查询待审核的设备（is_qualified = 2 表示待审核）
        QueryWrapper<DroneDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_qualified", 2)
                .eq("is_deleted", 0);
        
        // 添加机主ID筛选条件（如果有）
        if (ownerId != null) {
            queryWrapper.eq("owner_id", ownerId);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        IPage<DroneDevice> devicePage = page(page, queryWrapper);
        
        // 转换为DeviceDetailVO
        return devicePage.convert(device -> {
            // 复用getDeviceDetail方法的逻辑
            DeviceDetailVO detailVO = new DeviceDetailVO();
            
            // 设置设备基本信息
            DeviceDetailVO.DroneDeviceBasicInfo basicInfo = new DeviceDetailVO.DroneDeviceBasicInfo();
            basicInfo.setDeviceId(device.getDeviceId());
            basicInfo.setOwnerId(device.getOwnerId());
            basicInfo.setFlyerId(device.getFlyerId());
            basicInfo.setModel(device.getModel());
            basicInfo.setDeviceName(device.getDeviceName());
            basicInfo.setEndurance(device.getEndurance());
            basicInfo.setSerialNumber(device.getSerialNumber());
            basicInfo.setDeviceType(device.getDeviceType());
            basicInfo.setStatus(device.getStatus());
            basicInfo.setStatusDesc(DeviceStatusEnum.getByCode(device.getStatus()).getDesc());
            basicInfo.setManufacturer(device.getManufacturer());
            
            // 格式化时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (device.getPurchaseTime() != null) {
                basicInfo.setPurchaseTime(device.getPurchaseTime().format(formatter));
            }
            if (device.getLastMaintainTime() != null) {
                basicInfo.setLastMaintainTime(device.getLastMaintainTime().format(formatter));
            }
            
            // 确保isQualified有值，避免前端出现空值问题
            basicInfo.setIsQualified(device.getIsQualified() != null ? device.getIsQualified() : 0);
            basicInfo.setMaxLoad(device.getMaxLoad());
            
            detailVO.setDeviceInfo(basicInfo);
            
            // 查询最近的维护记录
            detailVO.setLastMaintainRecord(deviceMaintainService.getLastMaintainRecord(device.getDeviceId()));
            
            // 查询维护记录列表（最近5条）
            detailVO.setMaintainRecords(deviceMaintainService.getDeviceMaintainRecords(device.getDeviceId(), 1, 5).getRecords());
            
            return detailVO;
        });
    }
    
    @Override
    @Transactional
    public boolean auditDevice(Long deviceId, Integer status, Long adminId, String remark) {
        // 1. 校验设备是否存在
        DroneDevice device = getById(deviceId);
        if (device == null) {
            return false;
        }
        
        // 2. 校验审核状态参数
        if (status != 1 && status != 2) {
            return false;
        }
        
        // 3. 更新设备资质状态
        // 状态映射：1-通过 -> is_qualified=1，2-拒绝 -> is_qualified=0
        Integer isQualified = status == 1 ? 1 : 0;
        
        DroneDevice updateEntity = new DroneDevice();
        updateEntity.setDeviceId(deviceId);
        updateEntity.setIsQualified(isQualified);
        updateEntity.setUpdateTime(LocalDateTime.now());
        
        boolean result = updateById(updateEntity);
        if (result) {
            // 4. 记录操作日志
            DeviceOperateLog operateLog = new DeviceOperateLog();
            operateLog.setDeviceId(deviceId);
            operateLog.setOperatorId(adminId);
            operateLog.setBeforeStatus(device.getIsQualified());
            operateLog.setAfterStatus(isQualified);
            operateLog.setOperateTime(new Date());
            if (remark != null) {
                // 可以扩展DeviceOperateLog表添加remark字段来存储备注
                // operateLog.setRemark(remark);
            }
            deviceOperateLogMapper.insert(operateLog);
            
            // 5. 清除相关缓存
            redisTemplate.delete(DEVICE_STATUS_CACHE_KEY + deviceId);
            if (device.getFlyerId() != null) {
                redisTemplate.delete(FLYER_DEVICES_CACHE_KEY + device.getFlyerId());
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean deleteDevice(Long deviceId) {
        // 检查设备是否存在
        DroneDevice device = getById(deviceId);
        if (device == null) {
            return false;
        }
        
        // 检查设备是否被租借
        if (device.getFlyerId() != null) {
            // 设备被租借中，不允许删除
            throw new BusinessException("设备正在被租借中，无法删除");
        }
        
        // 执行删除操作
        return deviceMapper.deleteDeviceById(deviceId);
    }

    @Override
    public boolean OwnerUpdateDeviceById(DroneDevice device) {
        return deviceMapper.OwnerUpdateDeviceById(device);
    }
    
    @Override
    public List<Map<String, Object>> getFlyerRentalHistory(Long flyerId) {
        // 查询飞手的所有租借记录
        QueryWrapper<DeviceRental> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("flyer_id", flyerId)
                .orderByDesc("rental_start_time");
        
        List<DeviceRental> rentalRecords = deviceRentalService.list(queryWrapper);
        
        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DeviceRental rental : rentalRecords) {
            Map<String, Object> record = new HashMap<>();
            record.put("rentalId", rental.getRentalId());
            record.put("deviceId", rental.getDeviceId());
            
            // 查询设备信息
            DroneDevice device = getById(rental.getDeviceId());
            if (device != null) {
                record.put("deviceName", device.getDeviceName());
                record.put("deviceType", device.getDeviceType());
                record.put("picture", device.getPicture());
            }
            
            // 格式化时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");
            record.put("rentalStartTime", rental.getRentalStartTime() != null ? sdf.format(rental.getRentalStartTime()) : null);
            record.put("rentalEndTime", rental.getRentalEndTime() != null ? sdf.format(rental.getRentalEndTime()) : null);
            record.put("rentalStatus", rental.getRentalStatus());
            
            result.add(record);
        }
        
        return result;
    }
}
