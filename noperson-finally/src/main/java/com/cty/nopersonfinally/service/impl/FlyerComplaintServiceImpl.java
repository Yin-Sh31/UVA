package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.FlyerComplaintMapper;
import com.cty.nopersonfinally.mapper.UserFlyerMapper;
import com.cty.nopersonfinally.pojo.entity.FlyerComplaint;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.service.FlyerComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 飞手投诉服务实现类
 */
@Service
public class FlyerComplaintServiceImpl extends ServiceImpl<FlyerComplaintMapper, FlyerComplaint> implements FlyerComplaintService {

    @Autowired
    private FlyerComplaintMapper flyerComplaintMapper;

    @Autowired
    private UserFlyerMapper userFlyerMapper;

    @Override
    @Transactional
    public boolean addComplaint(FlyerComplaint complaint) {
        // 设置投诉时间
        complaint.setCreatedAt(LocalDateTime.now());
        
        // 保存投诉
        boolean result = save(complaint);
        
        if (result && complaint.getTargetId() != null) {
            // 更新飞手的投诉次数
            UserFlyer flyer = userFlyerMapper.selectById(complaint.getTargetId());
            if (flyer != null) {
                Integer complaintCount = flyer.getComplaintCount() != null ? flyer.getComplaintCount() : 0;
                flyer.setComplaintCount(complaintCount + 1);
                userFlyerMapper.updateById(flyer);
            }
        }
        
        return result;
    }

    @Override
    public List<FlyerComplaint> getByTargetId(Long targetId) {
        return flyerComplaintMapper.getByTargetId(targetId);
    }

    @Override
    @Transactional
    public boolean handleComplaint(Long id, String result) {
        FlyerComplaint complaint = getById(id);
        if (complaint != null) {
            complaint.setStatus(1);
            complaint.setResult(result);
            return updateById(complaint);
        }
        return false;
    }

    @Override
    public boolean hasComplained(Long orderId, Long reporterId) {
        FlyerComplaint complaint = flyerComplaintMapper.getByOrderAndReporter(orderId, reporterId);
        return complaint != null;
    }

    @Override
    public Integer countPendingComplaints() {
        return flyerComplaintMapper.countPendingComplaints();
    }
}
