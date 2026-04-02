// main/java/com/cty/nopersonfinally/service/impl/SprayDemandServiceImpl.java
package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.DemandMapper;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import com.cty.nopersonfinally.mapper.UserFlyerMapper;
import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;
import com.cty.nopersonfinally.pojo.entity.OrderDemand;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.pojo.enums.DeviceStatusEnum;
import com.cty.nopersonfinally.pojo.enums.OrderTypeEnum;
import com.cty.nopersonfinally.pojo.enums.SprayDemandStatusEnum;
import com.cty.nopersonfinally.pojo.vo.DemandVO;
import com.cty.nopersonfinally.service.AllDemandService;
import com.cty.nopersonfinally.service.DeviceService;
import com.cty.nopersonfinally.service.NotificationService;
import com.cty.nopersonfinally.service.SystemConfigService;
import com.cty.nopersonfinally.service.TransactionRecordService;
import com.cty.nopersonfinally.service.UserInfoService;
import com.cty.nopersonfinally.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class AllDemandServiceImpl extends ServiceImpl<DemandMapper, OrderDemand> implements AllDemandService {
    @Autowired
    private DemandMapper demandMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private UserFlyerMapper userFlyerMapper;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionRecordService transactionRecordService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private SystemConfigService systemConfigService;
    private static final Logger log = LoggerFactory.getLogger(AllDemandServiceImpl.class);
    @Override
    public Long createSprayDemand(OrderDemandDTO dto, Long farmerId) {
        // 复用新的createOrder方法
        return createOrder(dto, farmerId);
    }
    
    /**
     * 创建订单（支持喷洒和巡检）
     */
    @Override
    @Transactional
    public Long createOrder(OrderDemandDTO dto, Long farmerId) {
        // 参数校验
        if (dto.getOrderType() == null) {
            throw new BusinessException("订单类型不能为空");
        }
        if (dto.getLandArea() == null || dto.getLandArea() <= 0) {
            throw new BusinessException("地块面积必须大于0");
        }
        if (dto.getBudget() == null || dto.getBudget() <= 0) {
            throw new BusinessException("预算必须大于0");
        }
        
        // 根据订单类型进行不同的校验
        if (dto.getOrderType() == OrderTypeEnum.SPRAY.getCode()) {
            // 喷洒订单特有校验
            if (dto.getPestType() == null || dto.getPestType().trim().isEmpty()) {
                throw new BusinessException("病虫害类型不能为空");
            }
        } else if (dto.getOrderType() == OrderTypeEnum.INSPECTION.getCode()) {
            // 巡检订单特有校验
            if (dto.getInspectionPurpose() == null || dto.getInspectionPurpose().trim().isEmpty()) {
                throw new BusinessException("巡检目的不能为空");
            }
            if (dto.getExpectedResolution() == null) {
                throw new BusinessException("期望分辨率不能为空");
            }
        }

        // 1. 校验农户余额是否充足
        SysUser farmer = userMapper.selectById(farmerId);
        if (farmer == null) {
            throw new BusinessException("农户不存在");
        }
        
        Double budget = dto.getBudget();
        Double currentBalance = farmer.getBalance();
        if (currentBalance == null || currentBalance < budget) {
            throw new BusinessException("农户余额不足，请先充值");
        }

        // 2. 从农户余额中扣减费用
        Double newBalance = currentBalance - budget;
        farmer.setBalance(newBalance);
        userMapper.updateById(farmer);

        // 3. 创建需求记录（已支付状态）
        OrderDemand demand = new OrderDemand();
        demand.setFarmerId(farmerId);
        demand.setOrderType(dto.getOrderType());
        demand.setReportId(dto.getReportId()); // 喷洒订单必填，巡检订单可选
        demand.setLandId(dto.getLandId()); // 设置地块ID（可选）
        demand.setLandName(dto.getLandName());
        demand.setLandBoundary(dto.getLandBoundary());
        demand.setCropType(dto.getCropType());
        demand.setPestType(dto.getPestType()); // 喷洒订单必填
        demand.setLandLocation(dto.getLandLocation());
        demand.setLandArea(dto.getLandArea());
        demand.setExpectedTime(dto.getExpectedTime());
        demand.setBudget(dto.getBudget());
        demand.setPaymentAmount(dto.getBudget()); // 设置支付金额
        
        // 设置巡检特有字段
        demand.setInspectionPurpose(dto.getInspectionPurpose());
        demand.setExpectedResolution(dto.getExpectedResolution());
        
        demand.setStatus(SprayDemandStatusEnum.PENDING.getCode()); // 初始状态：待接取（已支付）
        demand.setPaymentStatus(1); // 1-已支付
        demand.setPaymentTime(LocalDateTime.now());
        demand.setPaymentMethod("balance"); // 余额支付
        demand.setCreateTime(LocalDateTime.now());
        demand.setUpdateTime(LocalDateTime.now());
        
        // 使用MyBatis-Plus的save方法，确保自动生成的ID能够回写
        this.save(demand);
        
        // 通知系统新需求创建
        notifySystemNewDemandCreated(demand.getDemandId());
        
        // 添加交易记录：农户支出
        transactionRecordService.addExpenseRecord(
            farmerId,
            budget,
            "创建订单支付" + (dto.getOrderType() == 1 ? "喷洒" : "巡检") + "费用",
            demand.getDemandId()
        );
        
        return demand.getDemandId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptPaidDemand(Long demandId, Long flyerId) {
        // 1. 记录操作开始日志
        log.info("开始处理已支付需求接单 - 需求ID: {}, 飞手ID: {}", demandId, flyerId);
        
        // 2. 检查全局飞手接单状态
        if (systemConfigService.isFlyerOrderBanned()) {
            log.warn("飞手接单已被全局禁止 - 需求ID: {}, 飞手ID: {}", demandId, flyerId);
            throw new BusinessException("由于天气问题，目前禁止接单！");
        }
        
        // 3. 检查飞手是否有未完成的订单
        if (hasUnfinishedOrder(flyerId)) {
            log.warn("飞手已有未完成订单 - 飞手ID: {}", flyerId);
            throw new BusinessException("您有未完成的订单，无法接新单");
        }
        
        // 4. 校验需求状态
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            log.error("需求不存在 - 需求ID: {}", demandId);
            throw new BusinessException("需求不存在");
        }
        
        log.info("获取到需求信息 - ID: {}, 当前状态: {}, 支付状态: {}", 
                 demandId, demand.getStatus(), demand.getPaymentStatus());
        
        if (demand.getStatus() != SprayDemandStatusEnum.PENDING.getCode()) {
            log.error("需求状态错误，不是待接取状态 - 需求ID: {}, 当前状态: {}", 
                      demandId, demand.getStatus());
            throw new BusinessException("需求状态错误，不是待接取状态");
        }
        if (demand.getPaymentStatus() != 1) {
            log.error("需求支付状态错误，不是已支付状态 - 需求ID: {}, 支付状态: {}", 
                      demandId, demand.getPaymentStatus());
            throw new BusinessException("需求支付状态错误，不是已支付状态");
        }

        // 3. 更新需求状态（乐观锁确保并发安全）
        OrderDemand updateDemand = new OrderDemand();
        updateDemand.setDemandId(demandId);
        updateDemand.setStatus(SprayDemandStatusEnum.WAITING_CONFIRM.getCode()); // 更新为待确认状态
        updateDemand.setFlyerId(flyerId);
        updateDemand.setAcceptTime(LocalDateTime.now());
        updateDemand.setUpdateTime(LocalDateTime.now());

        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("demand_id", demandId)
                .eq("status", SprayDemandStatusEnum.PENDING.getCode())
                .eq("payment_status", 1);

        log.info("执行需求状态更新 - 需求ID: {}, 更新状态为: {}", 
                 demandId, SprayDemandStatusEnum.WAITING_CONFIRM.getCode());
        
        int updateResult = demandMapper.update(updateDemand, queryWrapper);
        boolean updateSuccess = updateResult > 0;
        
        if (!updateSuccess) {
            log.warn("需求状态更新失败（可能已被其他飞手接单）- 需求ID: {}, 更新结果: {}", 
                     demandId, updateResult);
            return false;
        }
        
        log.info("需求状态更新成功 - 需求ID: {}, 更新结果: {}", demandId, updateResult);

        // 4. 二次确认数据库更新结果
        OrderDemand updatedDemand = getById(demandId);
        if (updatedDemand != null && updatedDemand.getStatus() == SprayDemandStatusEnum.WAITING_CONFIRM.getCode() 
                && updatedDemand.getFlyerId() != null && updatedDemand.getFlyerId().equals(flyerId)) {
            log.info("数据库更新二次确认成功 - 需求ID: {}, 新状态: {}, 新飞手ID: {}", 
                     demandId, updatedDemand.getStatus(), updatedDemand.getFlyerId());
        } else {
            log.error("数据库更新二次确认失败 - 需求ID: {}, 当前状态: {}, 当前飞手ID: {}", 
                     demandId, 
                     updatedDemand != null ? updatedDemand.getStatus() : "null", 
                     updatedDemand != null ? updatedDemand.getFlyerId() : "null");
            throw new BusinessException("接单更新失败，请重试");
        }

        // 5. 通知农户需求已被接取
        notifyFarmerDemandAccepted(demandId);
        
        log.info("已支付需求接单处理完成 - 需求ID: {}, 飞手ID: {}", demandId, flyerId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptDemand(Long demandId, Long flyerId) {
        // 1. 记录操作开始日志
        log.info("开始处理未支付需求接单 - 需求ID: {}, 飞手ID: {}", demandId, flyerId);
        
        // 2. 检查全局飞手接单状态
        if (systemConfigService.isFlyerOrderBanned()) {
            log.warn("飞手接单已被全局禁止 - 需求ID: {}, 飞手ID: {}", demandId, flyerId);
            throw new BusinessException("由于天气问题，目前禁止接单！");
        }
        
        // 3. 检查飞手是否有未完成的订单
        if (hasUnfinishedOrder(flyerId)) {
            log.warn("飞手已有未完成订单 - 飞手ID: {}", flyerId);
            throw new BusinessException("您有未完成的订单，无法接新单");
        }
        
        // 4. 校验需求状态
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            log.error("需求不存在 - 需求ID: {}", demandId);
            throw new BusinessException("需求不存在");
        }
        
        log.info("获取到需求信息 - ID: {}, 当前状态: {}", demandId, demand.getStatus());
        
        if (demand.getStatus() != SprayDemandStatusEnum.PENDING.getCode()) {
            log.error("需求状态错误，无法接取 - 需求ID: {}, 当前状态: {}", 
                      demandId, demand.getStatus());
            throw new BusinessException("需求状态错误，无法接取");
        }

        // 3. 更新需求状态（乐观锁确保并发安全）
        OrderDemand updateDemand = new OrderDemand();
        updateDemand.setDemandId(demandId);
        // 接单后将状态改为待确认
        updateDemand.setStatus(SprayDemandStatusEnum.WAITING_CONFIRM.getCode());
        updateDemand.setFlyerId(flyerId);
        updateDemand.setAcceptTime(LocalDateTime.now());
        updateDemand.setUpdateTime(LocalDateTime.now());
        // 设置支付相关信息
        updateDemand.setPaymentAmount(demand.getBudget()); // 默认支付金额为预算金额
        updateDemand.setPaymentStatus(0); // 0-未支付

        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("demand_id", demandId)
                .eq("status", SprayDemandStatusEnum.PENDING.getCode());

        log.info("执行需求状态更新 - 需求ID: {}, 更新状态为: {}, 支付金额: {}", 
                 demandId, SprayDemandStatusEnum.WAITING_CONFIRM.getCode(), demand.getBudget());
        
        int updateResult = demandMapper.update(updateDemand, queryWrapper);
        boolean updateSuccess = updateResult > 0;
        
        if (!updateSuccess) {
            log.warn("需求状态更新失败（可能已被其他飞手接单）- 需求ID: {}, 更新结果: {}", 
                     demandId, updateResult);
            return false;
        }
        
        log.info("需求状态更新成功 - 需求ID: {}, 更新结果: {}", demandId, updateResult);

        // 4. 二次确认数据库更新结果
        OrderDemand updatedDemand = getById(demandId);
        if (updatedDemand != null && updatedDemand.getStatus() == SprayDemandStatusEnum.WAITING_CONFIRM.getCode() 
                && updatedDemand.getFlyerId() != null && updatedDemand.getFlyerId().equals(flyerId)) {
            log.info("数据库更新二次确认成功 - 需求ID: {}, 新状态: {}, 新飞手ID: {}", 
                     demandId, updatedDemand.getStatus(), updatedDemand.getFlyerId());
        } else {
            log.error("数据库更新二次确认失败 - 需求ID: {}, 当前状态: {}, 当前飞手ID: {}", 
                     demandId, 
                     updatedDemand != null ? updatedDemand.getStatus() : "null", 
                     updatedDemand != null ? updatedDemand.getFlyerId() : "null");
            throw new BusinessException("接单更新失败，请重试");
        }

        // 5. 发送缴费通知给农户
        notifyFarmerPaymentRequired(demandId);
        
        log.info("未支付需求接单处理完成 - 需求ID: {}, 飞手ID: {}", demandId, flyerId);
        return true;
    }
    
    /**
     * 通知农户需要支付费用
     */
    private void notifyFarmerPaymentRequired(Long demandId) {
        OrderDemand demand = getById(demandId);
        if (demand != null) {
            // 创建缴费通知消息
            String message = String.format("您的需求#%d已被飞手接单，请及时支付费用%.2f元以继续后续流程。",
                    demandId, demand.getBudget());
            // 调用通知服务发送消息
            notificationService.sendNotification(
                    demand.getFarmerId(),
                    "缴费通知",
                    message,
                    "PAYMENT"
            );
        }
    }

    @Override
    @Transactional
    public boolean startWork(Long demandId, Long flyerId) {
        // 获取订单当前状态
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }
        
        // 如果订单是待接取状态(0)，先执行接单操作
        if (demand.getStatus() == SprayDemandStatusEnum.PENDING.getCode()) {
            // 检查支付状态，调用相应的接单方法
            boolean acceptSuccess;
            if (demand.getPaymentStatus() != null && demand.getPaymentStatus() == 1) {
                acceptSuccess = acceptPaidDemand(demandId, flyerId);
            } else {
                acceptSuccess = acceptDemand(demandId, flyerId);
            }
            
            if (!acceptSuccess) {
                return false;
            }
            
            // 接单成功后，再次获取更新后的需求
            demand = getById(demandId);
        }
        
        // 现在从WAITING_CONFIRM状态开始工作
        return updateDemandStatus(
                demandId,
                flyerId,
                SprayDemandStatusEnum.WAITING_CONFIRM.getCode(),
                SprayDemandStatusEnum.IN_PROGRESS.getCode(),
                null
        );
    }

    @Override
    @Transactional
    public boolean completeWork(Long demandId, Long flyerId) {
        // 添加详细日志
        log.info("飞手完成作业请求 - 需求ID: {}, 传入的飞手ID: {}", demandId, flyerId);
        
        // 先获取订单当前状态
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            log.error("需求不存在 - 需求ID: {}", demandId);
            throw new BusinessException("需求不存在");
        }
        
        // 打印订单详细信息用于调试
        log.info("订单详情 - 需求ID: {}, 订单飞手ID: {}, 订单状态: {}, 支付金额: {}, 创建时间: {}", 
                demand.getDemandId(), demand.getFlyerId(), demand.getStatus(), 
                demand.getPaymentAmount(), demand.getCreateTime());
        
        // 检查是否是当前飞手的订单
        if (demand.getFlyerId() == null) {
            log.error("订单未分配飞手 - 需求ID: {}, 传入的飞手ID: {}", demandId, flyerId);
            throw new BusinessException("订单未分配飞手，请先接单");
        }
        
        if (!demand.getFlyerId().equals(flyerId)) {
            log.error("权限验证失败 - 订单飞手ID: {}, 当前飞手ID: {}", 
                    demand.getFlyerId(), flyerId);
            // 提供更详细的错误信息，便于调试
            throw new BusinessException("无权操作此订单: 订单属于其他飞手");
        }
        
        // 检查需求状态是否正确
        Integer currentStatus = demand.getStatus();
        if (currentStatus != SprayDemandStatusEnum.WAITING_CONFIRM.getCode() && 
            currentStatus != SprayDemandStatusEnum.PROCESSING.getCode() && 
            currentStatus != SprayDemandStatusEnum.IN_PROGRESS.getCode()) {
            throw new BusinessException("需求状态错误，无法执行此操作");
        }
        
        // 构建更新对象
        OrderDemand updateDemand = new OrderDemand();
        updateDemand.setDemandId(demandId);
        updateDemand.setStatus(SprayDemandStatusEnum.COMPLETED.getCode());
        updateDemand.setCompleteTime(LocalDateTime.now());
        updateDemand.setUpdateTime(LocalDateTime.now());

        // 执行更新
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("demand_id", demandId)
                .eq("flyer_id", flyerId)
                .in("status", Arrays.asList(
                    SprayDemandStatusEnum.WAITING_CONFIRM.getCode(),
                    SprayDemandStatusEnum.PROCESSING.getCode(),
                    SprayDemandStatusEnum.IN_PROGRESS.getCode()
                ));

        boolean success = demandMapper.update(updateDemand, queryWrapper) > 0;

        if (success) {
            // 通知农户飞手完成作业
            notifyFarmerWorkCompleted(demandId);
            
            // 更新飞手钱包余额，无论订单是否已支付
            // 计算飞手收入（订单金额 - 平台手续费10%）
            Double orderAmount = demand.getPaymentAmount();
            if (orderAmount != null && orderAmount > 0) {
                Double platformFee = orderAmount * 0.1;
                Double flyerIncome = orderAmount - platformFee;
                
                // 更新飞手钱包余额（updateUserBalance方法内部已自动添加交易记录）
                userInfoService.updateUserBalance(flyerId, flyerIncome);
                
                // 记录日志
                log.info("飞手{}完成需求{}，获得收入{}元，已更新到钱包", flyerId, demandId, flyerIncome);
            }
            
            // 更新飞手完成订单数量
            updateFlyerCompletedOrders(flyerId);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean confirmCompletion(Long demandId, Long farmerId) {
        // 验证权限
        OrderDemand demand = getById(demandId);
        if (demand == null || !demand.getFarmerId().equals(farmerId)) {
            throw new BusinessException("无权限操作此需求");
        }
        
        // 从待确认状态更新为已完成
        boolean success = updateDemandStatus(
                demandId,
                farmerId,
                SprayDemandStatusEnum.WAITING_CONFIRM.getCode(),
                SprayDemandStatusEnum.COMPLETED.getCode(),
                null
        );
        
        if (success) {
            // 通知飞手需求已确认
            notifyFlyerDemandConfirmed(demandId);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean cancelDemand(Long demandId, Long operatorId, String reason) {
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }

        // 权限校验：农户可取消所有状态，飞手只能取消自己接的未完成需求
        boolean hasPermission = demand.getFarmerId().equals(operatorId) ||
                (demand.getFlyerId() != null && demand.getFlyerId().equals(operatorId) &&
                        demand.getStatus() < SprayDemandStatusEnum.COMPLETED.getCode());
        if (!hasPermission) {
            throw new BusinessException("无权限取消此需求");
        }

        // 记录原始支付状态和金额，用于后续退款处理
        boolean needRefund = (demand.getPaymentStatus() == 1 && demand.getPaymentAmount() != null && demand.getPaymentAmount() > 0);
        Double refundAmount = needRefund ? demand.getPaymentAmount() : 0;
        Long farmerId = demand.getFarmerId();

        // 执行取消
        boolean success = updateDemandStatus(
                demandId,
                operatorId,
                demand.getStatus(), // 当前状态
                SprayDemandStatusEnum.CANCELLED.getCode(),
                reason
        );

        if (success) {
            // 如果需求已支付，则退还金额到农户余额
            if (needRefund) {
                // 获取农户信息
                SysUser farmer = userMapper.selectById(farmerId);
                if (farmer != null) {
                    // 更新农户余额（退还支付金额）
                    Double currentBalance = farmer.getBalance() != null ? farmer.getBalance() : 0;
                    farmer.setBalance(currentBalance + refundAmount);
                    userMapper.updateById(farmer);
                    
                    log.info("需求取消退款：需求ID={}，农户ID={}，退款金额={}元", demandId, farmerId, refundAmount);
                    
                    // 添加交易记录：农户收入（退款）
                    transactionRecordService.addIncomeRecord(
                        farmerId,
                        refundAmount,
                        "订单取消退款 - " + (demand.getOrderType() == 1 ? "喷洒" : "巡检") + "订单",
                        demandId
                    );
                }
            }
            
            // 通知飞手需求被取消
            notifyFlyerDemandCancelled(demandId);
            
            // 释放设备
            if (demand.getDeviceId() != null) {
                deviceService.updateDeviceStatus(demand.getDeviceId(), DeviceStatusEnum.NORMAL, operatorId); // 0=IDLE
            }
        }
        return success;
    }

    @Override
    public Page<DemandVO> getFarmerDemands(Long farmerId, int pageNum, int pageSize, Integer status) {
        // 调用带类型筛选的方法，但类型为null（表示不筛选）
        return getFarmerDemandsByType(farmerId, pageNum, pageSize, status, null);
    }
    
    @Override
    public Page<DemandVO> getFarmerDemandsByType(Long farmerId, int pageNum, int pageSize, Integer status, Integer orderType) {
        Page<OrderDemand> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("farmer_id", farmerId)
                .orderByDesc("create_time");

        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        if (orderType != null) {
            queryWrapper.eq("order_type", orderType);
        }

        Page<OrderDemand> demandPage = demandMapper.selectPage(page, queryWrapper);
        return (Page<DemandVO>) demandPage.convert(this::convertToVO);
    }

    @Override
    public Page<DemandVO> getFlyerDemands(Long flyerId, int pageNum, int pageSize, Integer status) {
        Page<OrderDemand> page = new Page<>(pageNum, pageSize);
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("flyer_id", flyerId)
                .orderByDesc("accept_time");

        if (status != null) {
            queryWrapper.eq("status", status);
        }

        Page<OrderDemand> demandPage = demandMapper.selectPage(page, queryWrapper);
        return (Page<DemandVO>) demandPage.convert(this::convertToVO);
    }

    @Override
    public OrderDemand getDemandById(Long demandId, Long userId, String userRole) {
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }

        // 权限校验
        // 农户只能查看自己发布的需求
        if ("farmer".equals(userRole) && !demand.getFarmerId().equals(userId)) {
            throw new BusinessException("无权限查看此需求");
        }
        // 飞手可以查看所有需求（用于接单和查看详情）
        // 移除飞手的严格权限限制，让飞手能够查看所有农户发布的需求
        return demand;
    }

    @Override
    public void notifyFarmerDemandAccepted(Long demandId) {
        OrderDemand demand = getById(demandId);
        SysUser farmer = userMapper.selectById(demand.getFarmerId());
        if (farmer == null) {
            throw new BusinessException("关联农户不存在");
        }
        
        // 根据订单类型设置通知标题和内容
        String demandTypeDesc = OrderTypeEnum.getDescByCode(demand.getOrderType());
        String title = demandTypeDesc + "需求接取通知";
        String content = String.format("您的%s需求(#%d)已被飞手接取，即将开始作业", demandTypeDesc, demandId);

        notificationService.sendNotification(
                farmer.getUserId(),
                title,
                content,
                "DEMAND"
        );
    }

    @Override
    public void notifyFlyerDemandConfirmed(Long demandId) {
        OrderDemand demand = getById(demandId);
        SysUser flyer = userMapper.selectById(demand.getFlyerId());
        if (flyer == null) {
            throw new BusinessException("关联飞手不存在");
        }
        
        // 根据订单类型设置通知标题和内容
        String demandTypeDesc = OrderTypeEnum.getDescByCode(demand.getOrderType());
        String title = demandTypeDesc + "需求确认通知";
        String content = String.format("您的%s需求(#%d)已被农户确认完成，感谢您的服务", demandTypeDesc, demandId);

        notificationService.sendNotification(
                flyer.getUserId(),
                title,
                content,
                "DEMAND"
        );
    }
    
    @Override
    public void notifySystemNewDemandCreated(Long demandId) {
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }
        
        // 系统通知 - 新需求创建
        // 在实际应用中，这里应该有更复杂的逻辑来选择合适的飞手
        // 这里仅作为示例，我们假设系统会自动匹配飞手
        // 实际实现可以调用notificationService.sendOrderMatchNotification方法
        
        String demandTypeDesc = OrderTypeEnum.getDescByCode(demand.getOrderType());
        String message = String.format("系统已发布新的%s需求(#%d)，请查看匹配的飞手", demandTypeDesc, demandId);
        
        // 这里可以记录日志或者通知管理员
        System.out.println("系统通知: " + message);
    }
    
    @Override
    public void notifyFlyerDemandCancelled(Long demandId) {
        OrderDemand demand = getById(demandId);
        if (demand == null || demand.getFlyerId() == null) {
            return; // 需求不存在或没有飞手接单，无需通知
        }
        
        SysUser flyer = userMapper.selectById(demand.getFlyerId());
        if (flyer == null) {
            throw new BusinessException("关联飞手不存在");
        }
        
        String demandTypeDesc = OrderTypeEnum.getDescByCode(demand.getOrderType());
        String title = demandTypeDesc + "需求取消通知";
        String content = String.format("您接取的%s需求(#%d)已被取消，原因：%s", 
                demandTypeDesc, 
                demandId, 
                demand.getCancelReason() != null ? demand.getCancelReason() : "未提供原因");

        notificationService.sendNotification(
                flyer.getUserId(),
                title,
                content,
                "DEMAND_CANCELLED"
        );
    }
    
    @Override
    public void notifyFarmerWorkCompleted(Long demandId) {
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }
        
        SysUser farmer = userMapper.selectById(demand.getFarmerId());
        if (farmer == null) {
            throw new BusinessException("关联农户不存在");
        }
        
        String demandTypeDesc = OrderTypeEnum.getDescByCode(demand.getOrderType());
        String title = demandTypeDesc + "作业完成通知";
        String content = String.format("您的%s需求(#%d)已完成作业，请及时确认", demandTypeDesc, demandId);

        notificationService.sendNotification(
                farmer.getUserId(),
                title,
                content,
                "WORK_COMPLETED"
        );
    }
    
    /**
     * 支付需求费用
     * 注意：当前系统已实现发布需求时强制支付，此方法主要用于检查支付状态或处理特殊情况
     */
    @Override
    public boolean payDemand(Long demandId, Long farmerId) {
        // 1. 校验需求是否存在且属于当前农户
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }
        if (!demand.getFarmerId().equals(farmerId)) {
            throw new BusinessException("无权操作该需求");
        }
        
        // 2. 检查支付状态，当前系统中需求创建时已自动支付
        if (demand.getPaymentStatus() == 1) { // 1-已支付
            return true;
        }
        
        // 如果存在未支付的需求（可能是历史数据或特殊情况），返回错误
        throw new BusinessException("当前系统不支持后续支付，请重新创建需求");
    }
    
    /**
     * 通知飞手农户已完成支付
     */
    private void notifyFlyerPaymentCompleted(Long demandId) {
        OrderDemand demand = getById(demandId);
        if (demand != null && demand.getFlyerId() != null) {
            String message = String.format("农户已完成需求#%d的支付，金额%.2f元，请开始作业。",
                    demandId, demand.getPaymentAmount());
            notificationService.sendNotification(
                    demand.getFlyerId(),
                    "支付完成通知",
                    message,
                    "PAYMENT_COMPLETED"
            );
        }
    }

    @Override
    public Map<String, Integer> countByStatus() {
        Map<String, Integer> statusCount = new HashMap<>();
        int total = 0;

        // 获取所有喷洒需求状态枚举
        SprayDemandStatusEnum[] statusEnums = SprayDemandStatusEnum.values();
        for (SprayDemandStatusEnum status : statusEnums) {
            QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", status.getCode());
            int count = Math.toIntExact(demandMapper.selectCount(queryWrapper));
            // 以状态枚举名小写作为key（如pending、processing）
            statusCount.put(status.name().toLowerCase(), count);
            total += count;
        }

        // 增加总数量统计
        statusCount.put("total", total);
        return statusCount;
    }

    @Override
    public Page<DemandVO> getAdminDemandPage(int pageNum, int pageSize, Integer status) {
        // 创建分页对象
        Page<OrderDemand> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        // 按状态筛选（如果传入状态参数）
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        // 按创建时间倒序排序（最新的在前）
        queryWrapper.orderByDesc("create_time");

        // 执行分页查询
        Page<OrderDemand> demandPage = demandMapper.selectPage(page, queryWrapper);

        // 转换为VO对象并返回
        return (Page<DemandVO>) demandPage.convert(this::convertToVO);
    }
    
    @Override
    public Page<DemandVO> getOtherFarmersDemands(Long currentFarmerId, int pageNum, int pageSize, String landLocation, String cropType, Integer status) {
        // 创建分页对象
        Page<OrderDemand> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        // 排除当前农户自己的需求
        queryWrapper.ne("farmer_id", currentFarmerId);
        // 不展示取消的需求，但展示待处理和已完成的需求
        queryWrapper.notIn("status", SprayDemandStatusEnum.CANCELLED.getCode());
        
        // 按创建时间倒序排序（最新的在前）
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        Page<OrderDemand> demandPage = demandMapper.selectPage(page, queryWrapper);
        
        // 转换为VO对象并进行匿名化处理
        return (Page<DemandVO>) demandPage.convert(demand -> {
            DemandVO vo = this.convertToVO(demand);
            // 匿名化处理：隐藏农户ID和姓名等个人信息
            vo.setFarmerId(null);
            // 可以根据业务需求添加更多匿名化处理
            return vo;
        });
    }
    
    @Override
    public Page<DemandVO> getAllFarmersDemandsForFlyer(int pageNum, int pageSize, String skillLevel, String location, Integer status) {
        // 检查全局飞手接单状态
        if (systemConfigService.isFlyerOrderBanned()) {
            // 返回空列表，但不抛出异常，让前端显示友好提示
            Page<DemandVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setRecords(java.util.Collections.emptyList());
            emptyPage.setTotal(0);
            return emptyPage;
        }
        // 调用带类型筛选的方法，但类型为null（表示不筛选）
        return getFarmersDemandsByTypeForFlyer(pageNum, pageSize, skillLevel, location, status, null);
    }
    
    @Override
    public Page<DemandVO> getFarmersDemandsByTypeForFlyer(int pageNum, int pageSize, String skillLevel, String location, Integer status, Integer orderType) {
        // 创建分页对象
        Page<OrderDemand> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        
        // 默认为待接取状态的需求（如果没有指定状态）
        if (status == null) {
            queryWrapper.in("status", Arrays.asList(
                SprayDemandStatusEnum.PENDING.getCode()
            ));
        } else {
            queryWrapper.eq("status", status);
        }
        
        // 按订单类型筛选（如果传入参数）
        if (orderType != null) {
            queryWrapper.eq("order_type", orderType);
        }
        
        // 按技能等级筛选（如果传入参数）
        if (skillLevel != null && !skillLevel.isEmpty()) {
            queryWrapper.eq("skill_level", skillLevel);
        }
        
        // 按地理位置筛选（如果传入参数）
        if (location != null && !location.isEmpty()) {
            queryWrapper.like("land_location", location);
        }
        
        // 按创建时间倒序排序（最新的在前）
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        Page<OrderDemand> demandPage = demandMapper.selectPage(page, queryWrapper);
        
        // 转换为VO对象并进行匿名化处理（保护农户隐私）
        return (Page<DemandVO>) demandPage.convert(demand -> {
            DemandVO vo = this.convertToVO(demand);
            // 匿名化处理：隐藏农户ID等个人信息
            vo.setFarmerId(null);
            // 可以根据业务需求添加更多匿名化处理
            return vo;
        });
    }

    /**
     * 补充实体转VO的方法（原代码中已有调用，此处完善实现）
     */


    /**
     * 通用状态更新方法（封装重复逻辑）
     */
    private boolean updateDemandStatus(Long demandId, Long operatorId, int currentStatus, int targetStatus, String cancelReason) {
        OrderDemand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException("需求不存在");
        }

        // 校验当前状态是否匹配
        if (demand.getStatus() != currentStatus) {
            throw new BusinessException("需求状态错误，无法执行此操作");
        }

        // 构建更新对象
        OrderDemand updateDemand = new OrderDemand();
        updateDemand.setDemandId(demandId);
        updateDemand.setStatus(targetStatus);
        updateDemand.setUpdateTime(LocalDateTime.now());

        // 补充状态相关字段
        if (targetStatus == SprayDemandStatusEnum.IN_PROGRESS.getCode()) {
            updateDemand.setStartTime(LocalDateTime.now());
        } else if (targetStatus == SprayDemandStatusEnum.WAITING_CONFIRM.getCode()) {
            updateDemand.setCompleteTime(LocalDateTime.now());
        } else if (targetStatus == SprayDemandStatusEnum.COMPLETED.getCode()) {
            updateDemand.setCompleteTime(LocalDateTime.now());
        } else if (targetStatus == SprayDemandStatusEnum.CANCELLED.getCode()) {
            updateDemand.setCancelTime(LocalDateTime.now());
            updateDemand.setCancelReason(cancelReason);
        }

        // 执行更新
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("demand_id", demandId)
                .eq("status", currentStatus);

        return demandMapper.update(updateDemand, queryWrapper) > 0;
    }

    /**
     * 实体转VO
     */
    @Override
    public DemandVO convertToVO(OrderDemand demand) {
        DemandVO vo = new DemandVO();
        vo.setDemandId(demand.getDemandId());
        vo.setFarmerId(demand.getFarmerId());
        vo.setFlyerId(demand.getFlyerId());
        vo.setReportId(demand.getReportId());
        vo.setLandName(demand.getLandName());
        vo.setLandId(demand.getLandId()); // 设置地块ID
        vo.setLandLocation(demand.getLandLocation()); // 地块位置
        vo.setCropType(demand.getCropType());
        vo.setPestType(demand.getPestType());
        vo.setLandArea(demand.getLandArea());
        vo.setBudget(demand.getBudget());
        vo.setExpectedTime(demand.getExpectedTime());
        vo.setStatus(demand.getStatus()); // 调用setStatus方法会自动设置statusDesc
        vo.setAcceptTime(demand.getAcceptTime());
        vo.setCreateTime(demand.getCreateTime());
        // 设置订单类型
        vo.setOrderType(demand.getOrderType() != null ? demand.getOrderType() : 1); // 默认喷洒
        // 设置订单类型描述
        vo.setOrderTypeDesc(OrderTypeEnum.getDescByCode(vo.getOrderType()));
        // 设置巡检特有字段
        vo.setInspectionPurpose(demand.getInspectionPurpose());
        vo.setExpectedResolution(demand.getExpectedResolution());
        // 设置支付相关字段
        vo.setPaymentAmount(demand.getPaymentAmount());
        vo.setPaymentStatus(demand.getPaymentStatus());
        vo.setPaymentTime(demand.getPaymentTime());
        vo.setPaymentMethod(demand.getPaymentMethod());

        // 补充飞手姓名
        if (demand.getFlyerId() != null) {
            SysUser flyer = userMapper.selectById(demand.getFlyerId());
            if (flyer != null) {
                vo.setFlyerName(flyer.getUsername());
            }
        }
        // 补充农户姓名
        if (demand.getFarmerId() != null) {
            SysUser farmer = userMapper.selectById(demand.getFarmerId());
            if (farmer != null) {
                vo.setFarmerName(farmer.getUsername());
            }
        }
        return vo;
    }

    /**
     * 检查飞手是否有未完成的订单
     * @param flyerId 飞手ID
     * @return 是否有未完成的订单
     */
    private boolean hasUnfinishedOrder(Long flyerId) {
        QueryWrapper<OrderDemand> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("flyer_id", flyerId)
                .notIn("status", Arrays.asList(
                        SprayDemandStatusEnum.COMPLETED.getCode(),
                        SprayDemandStatusEnum.CANCELLED.getCode()
                ));
        return demandMapper.selectCount(queryWrapper) > 0;
    }
    
    /**
     * 更新飞手完成订单数量
     * @param flyerId 飞手用户ID
     */
    private void updateFlyerCompletedOrders(Long flyerId) {
        try {
            // 根据用户ID查询飞手信息
            UserFlyer flyer = userFlyerMapper.selectByUserId(flyerId);
            if (flyer != null) {
                // 获取当前完成订单数量，如果为null则默认为0
                Integer currentCount = flyer.getCompletedOrders() != null ? flyer.getCompletedOrders() : 0;
                // 完成订单数量加1
                flyer.setCompletedOrders(currentCount + 1);
                // 更新数据库
                userFlyerMapper.updateById(flyer);
                log.info("飞手{}完成订单数量已更新为{}", flyerId, currentCount + 1);
            } else {
                log.warn("未找到飞手信息 - 用户ID: {}", flyerId);
            }
        } catch (Exception e) {
            log.error("更新飞手完成订单数量失败 - 飞手ID: {}, 错误: {}", flyerId, e.getMessage());
        }
    }
}