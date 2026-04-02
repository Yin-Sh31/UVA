package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.mapper.DemandFeedbackMapper;
import com.cty.nopersonfinally.pojo.entity.DemandFeedback;
import com.cty.nopersonfinally.service.DemandFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 需求反馈服务实现类
 */
@Service
public class DemandFeedbackServiceImpl implements DemandFeedbackService {
    
    @Autowired
    private DemandFeedbackMapper demandFeedbackMapper;

    @Override
    public boolean createFeedback(DemandFeedback feedback) {
        feedback.setStatus("pending");
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());
        return demandFeedbackMapper.insert(feedback) > 0;
    }

    @Override
    public DemandFeedback getFeedbackById(Long id) {
        return demandFeedbackMapper.selectById(id);
    }

    @Override
    public IPage<DemandFeedback> getFeedbackPage(int pageNum, int pageSize, String status) {
        QueryWrapper<DemandFeedback> queryWrapper = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");
        
        Page<DemandFeedback> page = new Page<>(pageNum, pageSize);
        return demandFeedbackMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean approveFeedback(Long id, Long adminId, String adminName, String remark) {
        DemandFeedback feedback = demandFeedbackMapper.selectById(id);
        if (feedback == null) {
            return false;
        }
        
        feedback.setStatus("approved");
        feedback.setAdminId(adminId);
        feedback.setAdminName(adminName);
        feedback.setAuditTime(LocalDateTime.now());
        feedback.setAuditRemark(remark);
        feedback.setUpdateTime(LocalDateTime.now());
        
        return demandFeedbackMapper.updateById(feedback) > 0;
    }

    @Override
    public boolean rejectFeedback(Long id, Long adminId, String adminName, String remark) {
        DemandFeedback feedback = demandFeedbackMapper.selectById(id);
        if (feedback == null) {
            return false;
        }
        
        feedback.setStatus("rejected");
        feedback.setAdminId(adminId);
        feedback.setAdminName(adminName);
        feedback.setAuditTime(LocalDateTime.now());
        feedback.setAuditRemark(remark);
        feedback.setUpdateTime(LocalDateTime.now());
        
        return demandFeedbackMapper.updateById(feedback) > 0;
    }

    @Override
    public DemandFeedback getFeedbackByDemandId(Long demandId) {
        QueryWrapper<DemandFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("demand_id", demandId);
        return demandFeedbackMapper.selectOne(queryWrapper);
    }
}