package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.PaymentLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付日志Mapper
 */
@Mapper
public interface PaymentLogMapper extends BaseMapper<PaymentLog> {
}