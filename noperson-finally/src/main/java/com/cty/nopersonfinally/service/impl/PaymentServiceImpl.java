package com.cty.nopersonfinally.service.impl;


import com.cty.nopersonfinally.mapper.PaymentLogMapper;
import com.cty.nopersonfinally.mapper.PaymentOrderMapper;
import com.cty.nopersonfinally.pojo.dto.PaymentDTO;
import com.cty.nopersonfinally.pojo.entity.OrderDemand;
import com.cty.nopersonfinally.pojo.entity.PaymentLog;
import com.cty.nopersonfinally.pojo.entity.PaymentOrder;
import com.cty.nopersonfinally.pojo.vo.PaymentVO;
import com.cty.nopersonfinally.service.AllDemandService;
import com.cty.nopersonfinally.service.PaymentService;
import com.cty.nopersonfinally.utils.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 支付服务实现类
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    // 手动声明日志对象
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;
    
    @Autowired
    private PaymentLogMapper paymentLogMapper;
    
    @Autowired
    private AllDemandService allDemandService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO payDemand(Long demandId, Long userId) {
        try {
            log.info("处理支付请求 - 需求ID: {}, 用户ID: {}", demandId, userId);
            
            // 1. 查询需求信息（使用带权限校验的方法）
            OrderDemand demand = allDemandService.getDemandById(demandId, userId, "farmer");
            log.info("成功获取需求信息 - 需求ID: {}, 农户ID: {}", demandId, demand.getFarmerId());
            // 如果需求不存在或无权限，getDemandById方法会抛出异常，这里不需要重复检查
            
            // 3. 检查支付状态
            if (demand.getPaymentStatus() == 1) {
                throw new BusinessException("需求已支付");
            }
            
            // 4. 创建支付DTO
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setPaymentMethod("WECHAT"); // 默认微信支付
            
            // 5. 创建支付订单
            PaymentVO paymentVO = createPayment(
                paymentDTO,
                userId,
                demandId.toString(),
                demand.getOrderType() == 1 ? "SPRAY" : "INSPECTION",
                demand.getPaymentAmount() != null ? demand.getPaymentAmount() : demand.getBudget()
            );
            
            // 6. 记录支付日志
            logPayment(userId, demandId.toString(), demand.getOrderType() == 1 ? "SPRAY" : "INSPECTION", 
                demand.getPaymentAmount() != null ? demand.getPaymentAmount() : demand.getBudget(),
                "WAITING", "WECHAT", paymentVO.getPaymentId().toString(), "创建支付订单");
            
            return paymentVO;
        } catch (BusinessException e) {
            log.error("支付需求失败: {}. 需求ID: {}, 用户ID: {}", e.getMessage(), demandId, userId, e);
            // 抛出异常时增加更详细的信息，帮助调试
            if ("无权限查看此需求".equals(e.getMessage())) {
                throw new BusinessException("无权限查看此需求 - 请确认您是否为该需求的创建者");
            }
            throw e;
        } catch (Exception e) {
            log.error("支付需求系统错误: {}", e.getMessage(), e);
            throw new BusinessException("支付失败，请稍后重试");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(PaymentDTO dto, Long userId, String orderId, String orderType, Double amount) {
        try {
            // 1. 生成外部交易号
            String outTradeNo = generateOutTradeNo(userId, orderType);
            
            // 2. 创建支付订单
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setOrderId(orderId);
            paymentOrder.setOrderType(orderType);
            paymentOrder.setUserId(userId);
            paymentOrder.setAmount(BigDecimal.valueOf(amount));
            paymentOrder.setPaymentMethod(dto.getPaymentMethod());
            paymentOrder.setOutTradeNo(outTradeNo);
            paymentOrder.setStatus("WAITING");
            paymentOrder.setCreateTime(LocalDateTime.now());
            
            // 3. 保存支付订单
            if (paymentOrderMapper.insert(paymentOrder) <= 0) {
                throw new BusinessException("创建支付订单失败");
            }
            
            // 4. 构建返回结果
            PaymentVO paymentVO = new PaymentVO();
            paymentVO.setPaymentId(paymentOrder.getId());
            paymentVO.setPaymentMethod(paymentOrder.getPaymentMethod());
            paymentVO.setPayUrl("https://api.payment.example.com/pay?outTradeNo=" + outTradeNo); // 模拟支付链接
            
            return paymentVO;
        } catch (BusinessException e) {
            log.error("创建支付订单失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("创建支付订单系统错误: {}", e.getMessage(), e);
            throw new BusinessException("创建支付订单失败，请稍后重试");
        }
    }
    
    @Override
    public PaymentVO queryPaymentStatus(String orderType, String orderId, Long userId) {
        try {
            // 1. 查询支付订单
            PaymentOrder paymentOrder = paymentOrderMapper.selectByOrderIdAndType(orderId, orderType);
            if (paymentOrder == null) {
                throw new BusinessException("支付订单不存在");
            }
            
            // 2. 验证用户权限
            if (!paymentOrder.getUserId().equals(userId)) {
                throw new BusinessException("无权查询此支付状态");
            }
            
            // 3. 构建返回结果
            PaymentVO paymentVO = new PaymentVO();
            paymentVO.setPaymentId(paymentOrder.getId());
            paymentVO.setPaymentMethod(paymentOrder.getPaymentMethod());
            paymentVO.setStatus(paymentOrder.getStatus());
            paymentVO.setPayTime(paymentOrder.getPayTime());
            
            return paymentVO;
        } catch (BusinessException e) {
            log.error("查询支付状态失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("查询支付状态系统错误: {}", e.getMessage(), e);
            throw new BusinessException("查询支付状态失败，请稍后重试");
        }
    }
    
    /**
     * 生成外部交易号
     */
    public String generateOutTradeNo(Long userId, String orderType) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return orderType + "_" + userId + "_" + timestamp + "_" + uuid;
    }
    
    /**
     * 记录支付日志
     */
    private void logPayment(Long userId, String orderId, String orderType, Double amount, 
                           String status, String  paymentMethod, String paymentId, String remark) {
        PaymentLog paymentLog = new PaymentLog();
        paymentLog.setUserId(userId);
        paymentLog.setOrderId(orderId);
        paymentLog.setOrderType(orderType);
        paymentLog.setAmount(BigDecimal.valueOf(amount));
        paymentLog.setStatus(status);
        paymentLog.setPaymentMethod(paymentMethod);
        paymentLog.setTransactionId(paymentId);
        paymentLog.setRemark(remark);
        paymentLog.setCreateTime(LocalDateTime.now());
        paymentLogMapper.insert(paymentLog);
    }
}