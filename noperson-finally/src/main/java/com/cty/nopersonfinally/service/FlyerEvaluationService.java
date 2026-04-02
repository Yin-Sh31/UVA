package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.entity.FlyerEvaluation;

import java.util.List;

/**
 * 飞手评价服务接口
 */
public interface FlyerEvaluationService extends IService<FlyerEvaluation> {

    /**
     * 添加评价
     */
    boolean addEvaluation(FlyerEvaluation evaluation);

    /**
     * 根据飞手用户ID获取评价列表
     */
    List<FlyerEvaluation> getByFlyerId(Long userId);

    /**
     * 获取飞手好评率
     */
    Double getPositiveRate(Long userId);

    /**
     * 获取飞手评价总数
     */
    Integer getEvaluationCount(Long userId);

    /**
     * 更新飞手信用数据
     */
    boolean updateFlyerCredit(Long userId);

    /**
     * 检查是否已评价
     */
    boolean hasEvaluated(Long orderId, Long farmerId);
}
