package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.TransactionRecordMapper;
import com.cty.nopersonfinally.pojo.entity.TransactionRecord;
import com.cty.nopersonfinally.service.TransactionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 交易记录服务实现类
 * 实现用户收支明细的业务操作
 */
@Service
public class TransactionRecordServiceImpl extends ServiceImpl<TransactionRecordMapper, TransactionRecord> implements TransactionRecordService {

    @Autowired
    private TransactionRecordMapper transactionRecordMapper;

    @Override
    public Page<TransactionRecord> getUserTransactions(Long userId, int pageNum, int pageSize, 
                                                     LocalDateTime startTime, LocalDateTime endTime, 
                                                     String transactionType) {
        Page<TransactionRecord> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        QueryWrapper<TransactionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        
        if (startTime != null) {
            queryWrapper.ge("create_time", startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le("create_time", endTime);
        }
        
        if (transactionType != null && !transactionType.isEmpty()) {
            queryWrapper.eq("transaction_type", transactionType);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public Double getUserIncomeSum(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<TransactionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("transaction_type", "INCOME");
        
        if (startTime != null) {
            queryWrapper.ge("create_time", startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le("create_time", endTime);
        }
        
        Double sum = this.baseMapper.selectObjs(queryWrapper).stream()
                .mapToDouble(obj -> {
                    if (obj instanceof TransactionRecord) {
                        return ((TransactionRecord) obj).getAmount();
                    }
                    return 0.0;
                })
                .sum();
                
        return sum != null ? sum : 0.0;
    }

    @Override
    public Double getUserExpenseSum(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<TransactionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("transaction_type", "EXPENSE");
        
        if (startTime != null) {
            queryWrapper.ge("create_time", startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le("create_time", endTime);
        }
        
        Double sum = this.baseMapper.selectObjs(queryWrapper).stream()
                .mapToDouble(obj -> {
                    if (obj instanceof TransactionRecord) {
                        return ((TransactionRecord) obj).getAmount();
                    }
                    return 0.0;
                })
                .sum();
                
        return sum != null ? sum : 0.0;
    }

    @Transactional
    @Override
    public TransactionRecord addIncomeRecord(Long userId, Double amount, String description, Long relatedOrderId) {
        TransactionRecord record = new TransactionRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setTransactionType("INCOME");
        record.setStatus(1);
        record.setDescription(description);
        record.setRelatedDemandId(relatedOrderId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        this.save(record);
        return record;
    }

    @Transactional
    @Override
    public TransactionRecord addExpenseRecord(Long userId, Double amount, String description, Long relatedOrderId) {
        TransactionRecord record = new TransactionRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setTransactionType("EXPENSE");
        record.setStatus(1);
        record.setDescription(description);
        record.setRelatedDemandId(relatedOrderId);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        this.save(record);
        return record;
    }
}