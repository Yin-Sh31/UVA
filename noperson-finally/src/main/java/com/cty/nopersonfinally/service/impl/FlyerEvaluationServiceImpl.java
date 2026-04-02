package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.FlyerEvaluationMapper;
import com.cty.nopersonfinally.mapper.UserFlyerMapper;
import com.cty.nopersonfinally.pojo.entity.FlyerEvaluation;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.service.FlyerEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 飞手评价服务实现类
 */
@Service
public class FlyerEvaluationServiceImpl extends ServiceImpl<FlyerEvaluationMapper, FlyerEvaluation> implements FlyerEvaluationService {

    @Autowired
    private FlyerEvaluationMapper flyerEvaluationMapper;

    @Autowired
    private UserFlyerMapper userFlyerMapper;

    @Override
    @Transactional
    public boolean addEvaluation(FlyerEvaluation evaluation) {
        // 设置评价时间
        evaluation.setCreatedAt(LocalDateTime.now());
        
        // 保存评价
        boolean result = save(evaluation);
        
        if (result) {
            // 更新飞手信用数据
            updateFlyerCredit(evaluation.getFlyerId());
        }
        
        return result;
    }

    @Override
    public List<FlyerEvaluation> getByFlyerId(Long userId) {
        // 直接使用userId查询评价列表（flyer_evaluation表中的flyer_id字段存储的就是userId）
        return flyerEvaluationMapper.getByFlyerId(userId);
    }

    @Override
    public Double getPositiveRate(Long userId) {
        // 直接使用userId查询好评率（flyer_evaluation表中的flyer_id字段存储的就是userId）
        return flyerEvaluationMapper.calculatePositiveRate(userId);
    }

    @Override
    public Integer getEvaluationCount(Long userId) {
        // 直接使用userId查询评价总数（flyer_evaluation表中的flyer_id字段存储的就是userId）
        return flyerEvaluationMapper.countByFlyerId(userId);
    }

    @Override
    @Transactional
    public boolean updateFlyerCredit(Long userId) {
        // 先通过userId查询飞手信息（user_flyer.user_id关联sys_user.user_id）
        UserFlyer flyer = flyerEvaluationMapper.getFlyerByUserId(userId);
        if (flyer == null) {
            // 如果飞手不存在，记录日志但不抛出异常
            System.out.println("警告：用户ID " + userId + " 对应的飞手不存在，无法更新信用数据");
            return false;
        }
        
        // 直接使用userId查询评价数据（flyer_evaluation表中的flyer_id字段存储的就是userId）
        // 计算综合评分
        Double totalScore = flyerEvaluationMapper.calculateTotalScore(userId);
        
        // 计算好评率
        Double positiveRate = flyerEvaluationMapper.calculatePositiveRate(userId);
        
        // 获取评价总数
        Integer evaluationCount = flyerEvaluationMapper.countByFlyerId(userId);
        
        // 更新飞手信用数据
        flyer.setTotalScore(totalScore != null ? totalScore : 0.0);
        flyer.setStarLevel(totalScore != null ? (int) Math.round(totalScore) : 0);
        flyer.setEvaluationCount(evaluationCount != null ? evaluationCount : 0);
        flyer.setPositiveRate(positiveRate != null ? positiveRate : 0.0);
        
        return userFlyerMapper.updateById(flyer) > 0;
    }

    @Override
    public boolean hasEvaluated(Long orderId, Long farmerId) {
        FlyerEvaluation evaluation = flyerEvaluationMapper.getByOrderAndFarmer(orderId, farmerId);
        return evaluation != null;
    }
}
