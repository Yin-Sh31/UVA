package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 飞手评价实体类
 */
@Data
@TableName("flyer_evaluation")
public class FlyerEvaluation {
    /**
     * 评价ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID（关联demand.demand_id）
     */
    private Long orderId;

    /**
     * 飞手ID（关联sys_user.user_id）
     */
    private Long flyerId;

    /**
     * 农户ID（关联sys_user.user_id）
     */
    private Long farmerId;

    /**
     * 作业质量评分（1-5分）
     */
    private Integer scoreQuality;

    /**
     * 准时性评分（1-5分）
     */
    private Integer scorePunctuality;

    /**
     * 服务态度评分（1-5分）
     */
    private Integer scoreAttitude;

    /**
     * 作业效率评分（1-5分）
     */
    private Integer scoreEfficiency;

    /**
     * 文字评价
     */
    private String comment;

    /**
     * 评价时间
     */
    private LocalDateTime createdAt;
}
