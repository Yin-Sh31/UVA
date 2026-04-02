package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.AiDiagnosisHistoryMapper;
import com.cty.nopersonfinally.pojo.entity.AiDiagnosisHistory;
import com.cty.nopersonfinally.service.AiDiagnosisHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiDiagnosisHistoryServiceImpl extends ServiceImpl<AiDiagnosisHistoryMapper, AiDiagnosisHistory> implements AiDiagnosisHistoryService {
    
    @Override
    public boolean saveDiagnosisHistory(AiDiagnosisHistory history) {
        return save(history);
    }
    
    @Override
    public List<AiDiagnosisHistory> getDiagnosisHistoryByUserId(Long userId) {
        LambdaQueryWrapper<AiDiagnosisHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiDiagnosisHistory::getUserId, userId)
                .orderByDesc(AiDiagnosisHistory::getCreateTime);
        return list(queryWrapper);
    }
    
    @Override
    public IPage<AiDiagnosisHistory> getDiagnosisHistoryByUserId(Long userId, Integer pageNum, Integer pageSize) {
        Page<AiDiagnosisHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiDiagnosisHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiDiagnosisHistory::getUserId, userId)
                .orderByDesc(AiDiagnosisHistory::getCreateTime);
        return page(page, queryWrapper);
    }
}
