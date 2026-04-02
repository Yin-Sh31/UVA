package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.dto.AuditDTO;

import com.cty.nopersonfinally.pojo.dto.FlyerUpdateDTO;
import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.pojo.vo.DemandVO;
import com.cty.nopersonfinally.pojo.vo.InspectionOrderVO;
import com.cty.nopersonfinally.pojo.vo.UserFlyerVO;

import java.util.List;

import com.cty.nopersonfinally.pojo.vo.OwnerInfoVO;

public interface UserFlyerService extends IService<UserFlyer> {
    // 审核飞手资质
    void auditQualification(AuditDTO dto);

    // 根据用户ID查询飞手信息
    UserFlyer getByUserId(Long userId);

    // 更新用户审核状态
    void updateUserAuditStatus(Long userId, int status);

    // 飞手更新个人信息（位置、价格、是否空闲等）
    boolean updateFlyerInfo(FlyerUpdateDTO dto, Long userId);

    // 分页查询飞手（带条件）
    IPage<UserFlyer> getFlyerPage(int pageNum, int pageSize, String skillLevel, Integer auditStatus);

    // 根据技能和审核状态查询飞手
    List<UserFlyer> getBySkillAndAudit(String skillLevel, Integer auditStatus);
    
    // 获取飞手个人信息VO（不含敏感信息）
    UserFlyerVO getFlyerInfoVO(Long userId);
    
    // 查询所有机主列表
    List<OwnerInfoVO> getAllOwners();
    
    // 查询单个机主详情
    OwnerInfoVO getOwnerDetail(Long ownerId);

    DemandVO acceptOrder(Long orderId);

}