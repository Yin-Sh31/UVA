package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.dto.PaymentDTO;
import com.cty.nopersonfinally.pojo.entity.PaymentOrder;
import com.cty.nopersonfinally.pojo.vo.PaymentVO;

/**
 * 支付服务接口
 */
public interface PaymentService {
    
    /**
     * 支付需求费用
     * @param demandId 需求ID
     * @param userId 用户ID
     * @return 支付结果
     */
    PaymentVO payDemand(Long demandId, Long userId);
    
    /**
     * 创建支付订单
     * @param dto 支付DTO
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param orderType 订单类型
     * @param amount 支付金额
     * @return 支付订单
     */
    PaymentVO createPayment(PaymentDTO dto, Long userId, String orderId, String orderType, Double amount);
    
    /**
     * 查询支付状态
     * @param orderType 订单类型
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 支付状态信息
     */
    PaymentVO queryPaymentStatus(String orderType, String orderId, Long userId);
//
//    /**
//     * 更新支付订单状态
//     * @param outTradeNo 外部交易号
//     * @param status 支付状态
//     * @param transactionId 交易ID
//     * @return 更新结果
//     */
//    boolean updatePaymentStatus(String outTradeNo, String status, String transactionId);
//
    /**
     * 生成外部交易号
     * @param userId 用户ID
     * @param orderType 订单类型
     * @return 外部交易号
     */
    String generateOutTradeNo(Long userId, String orderType);
}