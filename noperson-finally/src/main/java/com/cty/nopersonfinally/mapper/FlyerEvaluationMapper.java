package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.FlyerEvaluation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 飞手评价Mapper接口
 */
@Mapper
public interface FlyerEvaluationMapper extends BaseMapper<FlyerEvaluation> {

    /**
     * 根据飞手ID获取评价列表
     */
    List<FlyerEvaluation> getByFlyerId(Long flyerId);

    /**
     * 根据飞手ID计算综合评分
     */
    Double calculateTotalScore(Long flyerId);

    /**
     * 根据飞手ID计算好评率
     */
    Double calculatePositiveRate(Long flyerId);

    /**
     * 根据飞手ID获取评价总数
     */
    Integer countByFlyerId(Long flyerId);

    /**
     * 根据订单ID和农户ID查询是否已评价
     */
    FlyerEvaluation getByOrderAndFarmer(Long orderId, Long farmerId);

    /**
     * 根据用户ID查询飞手信息
     */
    com.cty.nopersonfinally.pojo.entity.UserFlyer getFlyerByUserId(Long userId);
}
