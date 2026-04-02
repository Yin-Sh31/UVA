package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.entity.TransactionRecord;
import java.time.LocalDateTime;

/**
 * 交易记录服务接口
 * 提供用户收支明细的业务操作
 */
public interface TransactionRecordService extends IService<TransactionRecord> {

    /**
     * 查询用户的交易记录
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param transactionType 交易类型
     * @return 分页结果
     */
    Page<TransactionRecord> getUserTransactions(Long userId, int pageNum, int pageSize, 
                                               LocalDateTime startTime, LocalDateTime endTime, 
                                               String transactionType);

    /**
     * 统计用户指定时间段内的收入总额
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 收入总额
     */
    Double getUserIncomeSum(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户指定时间段内的支出总额
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支出总额
     */
    Double getUserExpenseSum(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 添加收入记录
     * @param userId 用户ID
     * @param amount 金额
     * @param description 描述
     * @param relatedOrderId 关联订单ID
     * @return 交易记录
     */
    TransactionRecord addIncomeRecord(Long userId, Double amount, String description, Long relatedOrderId);

    /**
     * 添加支出记录
     * @param userId 用户ID
     * @param amount 金额
     * @param description 描述
     * @param relatedOrderId 关联订单ID
     * @return 交易记录
     */
    TransactionRecord addExpenseRecord(Long userId, Double amount, String description, Long relatedOrderId);
}