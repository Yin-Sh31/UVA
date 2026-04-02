package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import com.cty.nopersonfinally.mapper.UserFlyerMapper;
import com.cty.nopersonfinally.pojo.dto.AuditDTO;
import com.cty.nopersonfinally.mapper.DeviceMapper;
import com.cty.nopersonfinally.pojo.dto.FlyerUpdateDTO;
import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.pojo.entity.OrderDemand;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.pojo.enums.DeviceStatusEnum;
import com.cty.nopersonfinally.pojo.enums.NotificationType;
import com.cty.nopersonfinally.pojo.vo.DemandVO;
import com.cty.nopersonfinally.pojo.vo.OwnerInfoVO;
import com.cty.nopersonfinally.pojo.vo.UserFlyerVO;
import com.cty.nopersonfinally.service.DeviceService;
import java.util.ArrayList;
import com.cty.nopersonfinally.service.NotificationService;
import com.cty.nopersonfinally.service.AllDemandService;
import com.cty.nopersonfinally.service.UserFlyerService;
import com.cty.nopersonfinally.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFlyerServiceImpl extends ServiceImpl<UserFlyerMapper, UserFlyer> implements UserFlyerService {
    @Autowired
    private UserFlyerMapper flyerMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private NotificationService notificationService; // 通知服务
    @Autowired
    private AllDemandService allDemandService;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private DeviceService deviceService;

    // 通知服务
    @Override
    @Transactional // 事务保证
    public void auditQualification(AuditDTO dto) {
        // 1. 根据userId查询飞手信息（userId作为targetId）
        // 先查询用户是否存在
        SysUser user = userMapper.selectById(dto.getTargetId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 再查询对应的飞手信息
        UserFlyer flyer = getByUserId(dto.getTargetId());
        if (flyer == null) {
            throw new BusinessException("飞手不存在");
        }
        
        // 2. 更新飞手审核状态（补全缺失字段）
        flyer.setAuditTime(LocalDateTime.now());
        flyer.setAuditResult(String.valueOf(dto.getAuditResult())); // 补充审核结果
        flyer.setAuditRemark(dto.getAuditRemark()); // 补充审核备注
        flyerMapper.updateById(flyer);

        // 3. 更新用户表审核状态
        Integer userAuditStatus = dto.getAuditResult() == 1 ? 1 : 2; // 1-通过 2-拒绝
        user.setAuditStatus(userAuditStatus);
        userMapper.updateById(user);

        // 3. 发送审核结果通知（小程序模板消息+站内信）
        String content = dto.getAuditResult() == 1 ?
                "您的飞手资质已审核通过，可开始接单" :
                "您的飞手资质审核未通过，原因：" + dto.getAuditRemark();
        notificationService.sendNotification(
                flyer.getUserId(),
                "资质审核结果",
                content,
                String.valueOf(NotificationType.MINIPROGRAM));
    }

    @Override
    public UserFlyer getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        return lambdaQuery()
                .eq(UserFlyer::getUserId, userId)
                .one();
    }

    @Override
    public void updateUserAuditStatus(Long userId, int status) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        // 校验状态合法性（0-待审核，1-通过，2-拒绝）
        if (status < 0 || status > 2) {
            throw new BusinessException("审核状态不合法");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAuditStatus(status);
        userMapper.updateById(user);
    }

    // 飞手更新个人信息（只更新UserFlyer表，不更新SysUser表中的基本信息）
    @Override
    @Transactional
    public boolean updateFlyerInfo(FlyerUpdateDTO dto, Long userId) {
        System.out.println("开始更新飞手信息 - userId: " + userId);
        System.out.println("更新数据: " + dto.toString());
        
        // 1. 查询用户是否存在
        SysUser sysUser = userMapper.selectById(userId);
        if (sysUser == null) {
            System.out.println("更新失败：用户不存在 - userId: " + userId);
            throw new BusinessException("用户不存在");
        }
        
        // 2. 查询飞手信息
        UserFlyer flyer = getByUserId(userId);
        if (flyer == null) {
            System.out.println("更新失败：飞手信息不存在 - userId: " + userId);
            throw new BusinessException("飞手信息不存在");
        }
        
        // 记录更新前状态
        System.out.println("更新前信息 - location: " + flyer.getLocation() + ", pricePerAcre: " + flyer.getPricePerAcre() + ", isFree: " + flyer.getIsFree());
        
        // 标志变量，标记是否需要更新
        boolean needUpdateUser = false;
        boolean needUpdateFlyer = false;
        
        // 更新SysUser表中的字段
        if (dto.getPhone() != null) {
            System.out.println("更新phone字段: " + dto.getPhone());
            sysUser.setPhone(dto.getPhone());
            needUpdateUser = true;
        }
        if (dto.getAvatar() != null) {
            System.out.println("更新avatar字段: " + dto.getAvatar());
            sysUser.setAvatar(dto.getAvatar());
            // 同时更新user_flyer表中的头像信息以保持一致
            flyer.setAvatar(dto.getAvatar());
            needUpdateUser = true;
            needUpdateFlyer = true;
        }
        
        // 更新UserFlyer表中的字段
        if (dto.getLocation() != null) {
            System.out.println("更新location字段: " + dto.getLocation());
            flyer.setLocation(dto.getLocation());
            needUpdateFlyer = true;
        }
        if (dto.getPricePerAcre() != null) {
            System.out.println("更新pricePerAcre字段: " + dto.getPricePerAcre());
            flyer.setPricePerAcre(dto.getPricePerAcre());
            needUpdateFlyer = true;
        }
        if (dto.getIsFree() != null) {
            System.out.println("更新isFree字段: " + dto.getIsFree());
            flyer.setIsFree(dto.getIsFree());
            needUpdateFlyer = true;
        }
        if (dto.getSkillLevel() != null) {
            System.out.println("更新skillLevel字段: " + dto.getSkillLevel());
            flyer.setSkillLevel(dto.getSkillLevel());
            needUpdateFlyer = true;
        }
        if (dto.getIntroduction() != null) {
            System.out.println("更新introduction字段: " + dto.getIntroduction());
            flyer.setIntroduction(dto.getIntroduction());
            needUpdateFlyer = true;
        }
        
        // 执行数据库更新
        boolean result = true;
        
        // 更新SysUser表
        if (needUpdateUser) {
            boolean userResult = userMapper.updateById(sysUser) > 0;
            System.out.println("SysUser表更新结果: " + userResult);
            result &= userResult;
        }
        
        // 更新UserFlyer表
        if (needUpdateFlyer) {
            flyer.setUpdateTime(LocalDateTime.now());
            boolean flyerResult = updateById(flyer);
            System.out.println("UserFlyer表更新结果: " + flyerResult);
            result &= flyerResult;
        }
        
        if (result) {
            // 更新成功后重新查询验证
            UserFlyer updatedFlyer = getByUserId(userId);
            SysUser updatedUser = userMapper.selectById(userId);
            System.out.println("更新后验证 - location: " + updatedFlyer.getLocation() + ", phone: " + updatedUser.getPhone() + ", avatar: " + updatedUser.getAvatar());
        }
        
        return result;
    }

    // 分页查询飞手
    @Override
    public IPage<UserFlyer> getFlyerPage(int pageNum, int pageSize, String skillLevel, Integer auditStatus) {
        // 创建普通QueryWrapper而不是LambdaQueryWrapper，避免列名歧义
        QueryWrapper<UserFlyer> queryWrapper = new QueryWrapper<>();
        if (skillLevel != null) {
            queryWrapper.eq("skill_level", skillLevel);
        }
        if (auditStatus != null) {
            queryWrapper.eq("audit_status", auditStatus);
        }
        queryWrapper.eq("is_deleted", 0)
                   .orderByDesc("reputation");
        IPage<UserFlyer> page = new Page<>(pageNum, pageSize);
        return flyerMapper.selectPageByCondition(page, queryWrapper);
    }

    // 根据技能和审核状态查询飞手
    @Override
    public List<UserFlyer> getBySkillAndAudit(String skillLevel, Integer auditStatus) {
        return flyerMapper.selectBySkillAndAudit(skillLevel, auditStatus);
    }
    
    // 获取飞手个人信息VO（不含敏感信息），查询sys_user和user_flyer两个表的并集数据
    @Override
    public UserFlyerVO getFlyerInfoVO(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 1. 查询用户基本信息（sys_user表）
        SysUser sysUser = userMapper.selectById(userId);
        if (sysUser == null) {
            return null;
        }
        
        // 2. 查询飞手专业信息（user_flyer表）
        UserFlyer flyer = getByUserId(userId);
        if (flyer == null) {
            return null;
        }
        
        // 3. 合并两个表的数据到VO对象中
        UserFlyerVO vo = new UserFlyerVO();
        // 从sys_user表获取基本信息
        vo.setUserName(sysUser.getRealName() != null && !sysUser.getRealName().isEmpty() ? sysUser.getRealName() : sysUser.getUsername());
        vo.setUserId(sysUser.getUserId());
        vo.setPhone(sysUser.getPhone());
        vo.setAvatar(sysUser.getAvatar());
        vo.setRoleType(sysUser.getRoleType());
        vo.setStatus(sysUser.getStatus());
        vo.setBalance(sysUser.getBalance());
        
        // 从user_flyer表获取专业信息
        vo.setIntroduction(flyer.getIntroduction());
        vo.setLicenseType(flyer.getLicenseType());
        vo.setLicenseNo(flyer.getLicenseNo());
        vo.setLicenseUrl(flyer.getLicenseUrl());
        vo.setInsuranceNo(flyer.getInsuranceNo());
        vo.setInsuranceUrl(flyer.getInsuranceUrl());
        vo.setSkillLevel(flyer.getSkillLevel());
        vo.setLocation(flyer.getLocation());
        vo.setReputation(flyer.getReputation());
        vo.setPricePerAcre(flyer.getPricePerAcre());
        vo.setIsFree(flyer.getIsFree());
        vo.setAuditStatus(flyer.getAuditStatus()); // 审核状态从user_flyer表获取
        vo.setAuditResult(flyer.getAuditResult());
        vo.setAuditRemark(flyer.getAuditRemark());
        vo.setQualifications(flyer.getQualifications());
        vo.setCreditScore(flyer.getCreditScore());
        vo.setExperience(flyer.getExperience());
        vo.setCompletedOrders(flyer.getCompletedOrders());
        vo.setTotalScore(flyer.getTotalScore());
        vo.setStarLevel(flyer.getStarLevel());
        vo.setEvaluationCount(flyer.getEvaluationCount());
        vo.setPositiveRate(flyer.getPositiveRate());
        
        System.out.println("飞手信息获取成功 - 用户ID: " + userId);
        return vo;
    }
    
    @Override
    public List<OwnerInfoVO> getAllOwners() {
        // 查询所有机主用户（role_type=3）
        QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
        userQuery.eq("role_type", 3) // 3表示机主
                 .eq("is_deleted", 0);
        List<SysUser> owners = userMapper.selectList(userQuery);
        
        List<OwnerInfoVO> result = new ArrayList<>();
        for (SysUser owner : owners) {
            OwnerInfoVO vo = new OwnerInfoVO();
            vo.setUserId(owner.getUserId());
            vo.setCompanyName(owner.getRealName()); // 使用real_name作为公司名称
            
            // 查询该机主的设备信息
            QueryWrapper<DroneDevice> deviceQuery = new QueryWrapper<>();
            deviceQuery.eq("owner_id", owner.getUserId())
                      .eq("is_deleted", 0);
            List<DroneDevice> devices = deviceMapper.selectList(deviceQuery);
            vo.setDeviceTotal(devices.size());
            
            // 计算可用设备数量（状态为空闲的设备）
            long availableCount = devices.stream()
                .filter(device -> DeviceStatusEnum.NORMAL.getCode() == device.getStatus())
                .count();
            vo.setAvailableDeviceCount((int) availableCount);
            
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public OwnerInfoVO getOwnerDetail(Long ownerId) {
        // 查询机主用户信息
        SysUser owner = userMapper.selectById(ownerId);
        if (owner == null || owner.getRoleType() != 3) { // 3表示机主
            return null;
        }
        
        OwnerInfoVO vo = new OwnerInfoVO();
        vo.setUserId(owner.getUserId());
        vo.setCompanyName(owner.getRealName()); // 使用real_name作为公司名称
        
        // 查询该机主的所有设备
        QueryWrapper<DroneDevice> deviceQuery = new QueryWrapper<>();
        deviceQuery.eq("owner_id", ownerId)
                  .eq("is_deleted", 0);
        List<DroneDevice> devices = deviceMapper.selectList(deviceQuery);
        
        vo.setDeviceList(devices);
        vo.setDeviceTotal(devices.size());
        
        // 计算可用设备数量（状态为空闲的设备）
        long availableCount = devices.stream()
            .filter(device -> DeviceStatusEnum.NORMAL.getCode() == device.getStatus())
            .count();
        vo.setAvailableDeviceCount((int) availableCount);
        
        return vo;
    }

    @Override
    @Transactional
    public DemandVO acceptOrder(Long orderId) {
        //1.先获取订单id
        OrderDemand order = allDemandService.getById(orderId);
        //2.判断订单是否存在
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        //3.判断订单状态是否为待接单
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态错误");
        }
        //4.更新订单状态为待执行
        order.setStatus(2);
        //5.将更新保存到数据库
        boolean updateSuccess = allDemandService.updateById(order);
        if (!updateSuccess) {
            throw new BusinessException("更新订单状态失败");
        }
        //6.转换并返回VO对象
        DemandVO demandVO = new DemandVO();
        BeanUtils.copyProperties(order, demandVO);
        return demandVO;
    }


}