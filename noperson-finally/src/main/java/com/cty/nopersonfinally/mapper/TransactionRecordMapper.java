package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.entity.TransactionRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 交易记录Mapper接口
 * 提供交易记录的数据库操作
 */
public interface TransactionRecordMapper extends BaseMapper<TransactionRecord> {
    
    /**
     * 查询用户的交易记录
     * @param page 分页对象
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param transactionType 交易类型
     * @return 分页结果
     */
    Page<TransactionRecord> selectUserTransactions(
            Page<TransactionRecord> page,
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("transactionType") String transactionType
    );
    
    /**
     * 统计用户指定时间段内的收入总额
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 收入总额
     */
    Double selectUserIncomeSum(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 统计用户指定时间段内的支出总额
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 支出总额
     */
    Double selectUserExpenseSum(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}