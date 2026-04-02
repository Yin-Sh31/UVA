package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单Mapper
 */
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
    
    /**
     * 根据外部交易号查询订单
     * @param outTradeNo 外部交易号
     * @return 支付订单
     */
    PaymentOrder selectByOutTradeNo(String outTradeNo);
    
    /**
     * 根据订单ID和类型查询订单
     * @param orderId 订单ID
     * @param orderType 订单类型
     * @return 支付订单
     */
    PaymentOrder selectByOrderIdAndType(String orderId, String orderType);
}