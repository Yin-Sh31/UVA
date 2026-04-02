// main/java/com/cty/nopersonfinally/service/SprayDemandService.java
package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;
import com.cty.nopersonfinally.pojo.vo.DemandVO;

import java.util.Map;

public interface AllDemandService extends IService<com.cty.nopersonfinally.pojo.entity.OrderDemand> {
    // 创建喷洒需求
    Long createSprayDemand(OrderDemandDTO dto, Long farmerId);
    
    // 创建订单（支持喷洒和巡检）
    Long createOrder(OrderDemandDTO dto, Long farmerId);

    // 飞手接取需求（未支付状态）
    boolean acceptDemand(Long demandId, Long flyerId);
    
    // 飞手接取已支付需求
    boolean acceptPaidDemand(Long demandId, Long flyerId);

    // 开始喷洒作业
    boolean startWork(Long demandId, Long flyerId);

    // 完成喷洒作业
    boolean completeWork(Long demandId, Long flyerId);

    // 农户确认完成
    boolean confirmCompletion(Long demandId, Long farmerId);

    // 取消需求
    boolean cancelDemand(Long demandId, Long operatorId, String reason);

    // 分页查询农户的喷洒需求
    Page<DemandVO> getFarmerDemands(Long farmerId, int pageNum, int pageSize, Integer status);
    
    // 分页查询农户的需求列表（支持按订单类型筛选）
    Page<DemandVO> getFarmerDemandsByType(Long farmerId, int pageNum, int pageSize, Integer status, Integer orderType);

    // 分页查询飞手的喷洒需求
    Page<DemandVO> getFlyerDemands(Long flyerId, int pageNum, int pageSize, Integer status);

    // 获取需求详情（带权限校验）
    com.cty.nopersonfinally.pojo.entity.OrderDemand getDemandById(Long demandId, Long userId, String userRole);

    // 通知农户需求被接取
    void notifyFarmerDemandAccepted(Long demandId);

    // 通知飞手需求已确认
    void notifyFlyerDemandConfirmed(Long demandId);
    
    // 通知系统新需求创建（用于推送消息给合适的飞手）
    void notifySystemNewDemandCreated(Long demandId);
    
    // 通知飞手需求被取消
    void notifyFlyerDemandCancelled(Long demandId);
    
    // 通知农户飞手完成作业
    void notifyFarmerWorkCompleted(Long demandId);

    Map<String, Integer> countByStatus();

    Page<?> getAdminDemandPage(int pageNum, int pageSize, Integer status);
    
    // 实体转VO
    DemandVO convertToVO(com.cty.nopersonfinally.pojo.entity.OrderDemand demand);
    
    // 农户查看其他农户需求列表（匿名化处理）
    Page<DemandVO> getOtherFarmersDemands(Long currentFarmerId, int pageNum, int pageSize, String landLocation, String cropType, Integer status);
    
    // 飞手查询所有农户需求列表（主要是待接取的需求）
    Page<DemandVO> getAllFarmersDemandsForFlyer(int pageNum, int pageSize, String skillLevel, String location, Integer status);
    
    // 飞手按类型查询农户需求列表
    Page<DemandVO> getFarmersDemandsByTypeForFlyer(int pageNum, int pageSize, String skillLevel, String location, Integer status, Integer orderType);
    
    // 农户支付需求费用（从余额中扣减）
    boolean payDemand(Long demandId, Long farmerId);
}