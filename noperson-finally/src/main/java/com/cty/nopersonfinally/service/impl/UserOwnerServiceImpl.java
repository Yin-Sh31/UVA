package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.DeviceMapper;
import com.cty.nopersonfinally.mapper.UserOwnerMapper;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import com.cty.nopersonfinally.pojo.dto.AuditDTO;
import com.cty.nopersonfinally.pojo.dto.UserOwnerDTO;
import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.entity.UserOwner;
import com.cty.nopersonfinally.pojo.enums.NotificationType;
import com.cty.nopersonfinally.pojo.vo.UserOwnerBasicVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerDetailVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerDeviceStatVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerVO;
import com.cty.nopersonfinally.service.NotificationService;
import com.cty.nopersonfinally.service.UserOwnerService;
import com.cty.nopersonfinally.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class UserOwnerServiceImpl extends ServiceImpl<UserOwnerMapper, UserOwner> implements UserOwnerService {

    private static final Logger log = LoggerFactory.getLogger(UserOwnerServiceImpl.class);

    @Autowired
    private UserOwnerMapper ownerMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private NotificationService notificationService; // 通知服务（假设已存在）
    @Autowired
    private DeviceMapper deviceMapper;

    /**
     * 提交机主资质资料
     */
    @Override
    @Transactional
    public void submitQualification(UserOwnerDTO dto, Long userId) {
        // 1. 查询机主信息（不存在则创建，存在则更新）
        UserOwner owner = getByUserId(userId);
        if (owner == null) {
            owner = new UserOwner();
            owner.setUserId(userId);
            owner.setDeviceTotal(0);
            owner.setAvailableDeviceCount(0);
            owner.setCreditScore(100); // 初始信誉分
            owner.setCreateTime(LocalDateTime.now());
        }

        // 2. 更新资质信息
        owner.setLicenseType(dto.getLicenseType());
        owner.setLicenseNumber(dto.getLicenseNumber());
        owner.setLicenseUrls(String.join(",", dto.getLicenseUrls()));
        owner.setCommonArea(dto.getCommonArea());
        owner.setAuditStatus(0); // 重置为待审核
        owner.setRejectReason(null); // 清空拒绝原因
        owner.setUpdateTime(LocalDateTime.now());

        // 3. 保存信息
        if (owner.getId() == null) {
            ownerMapper.insert(owner);
        } else {
            ownerMapper.updateById(owner);
        }

        // 4. 更新用户表审核状态为待审核
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAuditStatus(0);
        sysUserMapper.updateById(user);
    }

    /**
     * 机主审核处理
     */
    @Override
    @Transactional
    public void auditQualification(AuditDTO dto) {
        // 1. 查询机主信息
        UserOwner owner = ownerMapper.selectById(dto.getTargetId());
        if (owner == null) {
            throw new BusinessException("机主信息不存在");
        }

        // 2. 更新机主审核信息
        owner.setAuditStatus(dto.getAuditResult());
        owner.setAuditorId(dto.getAuditorId());
        owner.setAuditTime(LocalDateTime.now());
        if (dto.getAuditResult() == 2) { // 审核拒绝
            owner.setRejectReason(dto.getAuditRemark());
        } else {
            owner.setRejectReason(null);
        }
        ownerMapper.updateById(owner);

        // 3. 更新用户表审核状态
        SysUser user = sysUserMapper.selectById(owner.getUserId());
        if (user == null) {
            throw new BusinessException("关联用户不存在");
        }
        user.setAuditStatus(dto.getAuditResult());
        sysUserMapper.updateById(user);

        // 4. 发送审核结果通知
        String content = dto.getAuditResult() == 1 ?
                "您的机主资质已审核通过，可开始管理设备" :
                "您的机主资质审核未通过，原因：" + dto.getAuditRemark();
        notificationService.sendNotification(
                owner.getUserId(),
                "资质审核结果",
                content,
                String.valueOf(NotificationType.MINIPROGRAM)
        );
    }

    /**
     * 根据用户ID查询机主信息
     */
    @Override
    public UserOwner getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        LambdaQueryWrapper<UserOwner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserOwner::getUserId, userId);
        return ownerMapper.selectOne(wrapper);
    }

    /**
     * 查询机主详情VO
     */
    @Override
    public UserOwnerDetailVO getDetailByUserId(Long userId) {
        UserOwnerDetailVO detailVO = ownerMapper.selectByUserId(userId);
        if (detailVO == null) {
            throw new BusinessException("机主信息不存在");
        }
        // 补充执照类型描述
        detailVO.setLicenseTypeDesc("INDIVIDUAL".equals(detailVO.getLicenseType()) ? "个人" : "企业");
        // 补充审核状态描述
        switch (detailVO.getAuditStatus()) {
            case 0: detailVO.setAuditStatusDesc("待审核"); break;
            case 1: detailVO.setAuditStatusDesc("已通过"); break;
            case 2: detailVO.setAuditStatusDesc("已拒绝"); break;
            default: detailVO.setAuditStatusDesc("未知状态");
        }
        return detailVO;
    }

    /**
     * 分页查询机主列表
     */
    @Override
    public IPage<UserOwnerVO> getOwnerPage(int pageNum, int pageSize, String area, Integer auditStatus) {
        Page<UserOwner> page = new Page<>(pageNum, pageSize);
        QueryWrapper<UserOwner> queryWrapper = new QueryWrapper<>();
        
        // 如果搜索关键词不为空，搜索姓名、电话或经营区域
        if (area != null && !area.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("su.real_name", area)
                .or()
                .like("su.phone", area)
                .or()
                .like("uo.common_area", area)
            );
        }
        
        queryWrapper.eq(auditStatus != null, "uo.audit_status", auditStatus)
                .eq("uo.is_deleted", 0)
                .orderByDesc("uo.create_time");
        return ownerMapper.selectOwnerPage(page, queryWrapper);
    }

    /**
     * 更新设备数量
     */
    @Override
    @Transactional
    public void updateDeviceCounts(Long ownerId, int totalChange, int availableChange) {
        UserOwner owner = ownerMapper.selectById(ownerId);
        if (owner == null) {
            throw new BusinessException("机主不存在");
        }
        // 计算新数量（确保不为负数）
        int newTotal = Math.max(0, owner.getDeviceTotal() + totalChange);
        int newAvailable = Math.max(0, owner.getAvailableDeviceCount() + availableChange);
        // 可用数不能超过总数
        newAvailable = Math.min(newAvailable, newTotal);

        owner.setDeviceTotal(newTotal);
        owner.setAvailableDeviceCount(newAvailable);
        owner.setUpdateTime(LocalDateTime.now());
        ownerMapper.updateById(owner);
    }

    /**
     * 获取设备统计信息
     */
    @Override
    public UserOwnerDeviceStatVO getDeviceStat(Long ownerId) {
        try {
            // 根据ownerId查询机主信息
            UserOwner owner = ownerMapper.selectById(ownerId);
            if (owner == null) {
                throw new BusinessException("机主不存在");
            }
            
            // 获取机主对应的用户ID（sys_user表的id）
            Long userId = owner.getUserId();
            if (userId == null) {
                throw new BusinessException("机主用户ID不存在");
            }
            
            // 从设备表实际统计设备数量
            List<DroneDevice> devices = deviceMapper.selectList(null);
            if (devices == null) {
                devices = new ArrayList<>();
            }
            
            // 过滤出当前用户的设备（DroneDevice的ownerId关联的是sys_user表的userId）
            List<DroneDevice> ownerDevices = devices.stream()
                    .filter(device -> device != null && userId.equals(device.getOwnerId()))
                    .collect(Collectors.toList());
            
            // 统计各状态设备数量
            int total = ownerDevices.size();
            int available = (int) ownerDevices.stream()
                    .filter(device -> device.getStatus() != null && device.getStatus() == 1)
                    .count();
            int maintaining = (int) ownerDevices.stream()
                    .filter(device -> device.getStatus() != null && device.getStatus() == 2)
                    .count();
            int working = total - available - maintaining;
            
            // 统计设备类型分布 - 修复处理deviceType字段的逻辑
            Map<String, Integer> typeDistribution = new HashMap<>();
            try {
                // 使用更安全的方式处理设备类型分布
                for (DroneDevice device : ownerDevices) {
                    if (device != null) {
                        String deviceType = device.getDeviceType();
                        String typeName = deviceType != null && !deviceType.trim().isEmpty() ? deviceType : "未知类型";
                        typeDistribution.put(typeName, typeDistribution.getOrDefault(typeName, 0) + 1);
                    }
                }
                
                // 如果没有类型分布数据，提供默认数据
                if (typeDistribution.isEmpty()) {
                    typeDistribution.put("巡检无人机", total > 0 ? total / 2 : 0);
                    typeDistribution.put("喷洒无人机", total > 0 ? total - (total / 2) : 0);
                }
            } catch (Exception e) {
                // 如果统计类型分布出错，提供默认数据
                typeDistribution.clear();
                typeDistribution.put("巡检无人机", 0);
                typeDistribution.put("喷洒无人机", 0);
            }

            UserOwnerDeviceStatVO statVO = new UserOwnerDeviceStatVO();
            statVO.setOwnerId(ownerId);
            statVO.setOwnerName(owner.getRealName() != null ? owner.getRealName() : "未设置");
            statVO.setTotal(total);
            statVO.setAvailable(available);
            statVO.setWorking(working);
            statVO.setMaintaining(maintaining);
            statVO.setTypeDistribution(typeDistribution);
            return statVO;
        } catch (BusinessException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            // 捕获所有其他异常，避免接口返回500错误
            log.error("获取设备统计信息失败: ", e);
            // 返回一个默认的统计对象
            UserOwnerDeviceStatVO defaultStat = new UserOwnerDeviceStatVO();
            defaultStat.setOwnerId(ownerId);
            defaultStat.setOwnerName("未知");
            defaultStat.setTotal(0);
            defaultStat.setAvailable(0);
            defaultStat.setWorking(0);
            defaultStat.setMaintaining(0);
            Map<String, Integer> defaultDistribution = new HashMap<>();
            defaultDistribution.put("巡检无人机", 0);
            defaultDistribution.put("喷洒无人机", 0);
            defaultStat.setTypeDistribution(defaultDistribution);
            return defaultStat;
        }
    }

    /**
     * 更新常用区域
     */
    @Override
    @Transactional
    public void updateCommonArea(String commonArea, Long userId) {
        UserOwner owner = getByUserId(userId);
        if (owner == null) {
            throw new BusinessException("机主信息不存在");
        }
        owner.setCommonArea(commonArea);
        owner.setUpdateTime(LocalDateTime.now());
        ownerMapper.updateById(owner);
    }
}