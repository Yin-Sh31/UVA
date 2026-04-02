package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 飞手投诉实体类
 */
@Data
@TableName("flyer_complaint")
public class FlyerComplaint {
    /**
     * 投诉ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 投诉人ID（农户/飞手）
     */
    private Long reporterId;

    /**
     * 被投诉人ID
     */
    private Long targetId;

    /**
     * 投诉原因
     */
    private String reason;

    /**
     * 处理状态（0-待处理 1-已处理）
     */
    private Integer status;

    /**
     * 处理结果
     */
    private String result;

    /**
     * 投诉时间
     */
    private LocalDateTime createdAt;
}
