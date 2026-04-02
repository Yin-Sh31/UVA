package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 农户常用地址实体类
 */
@Data
@TableName("farmer_favorite_address")
public class FarmerFavoriteAddress {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 农户ID
     */
    private Long farmerId;

    /**
     * 需求类型：1-喷洒，2-巡检
     */
    private Integer orderType;

    /**
     * 地块名称
     */
    private String landName;

    /**
     * 地块边界（经纬度）
     */
    private String landBoundary;

    /**
     * 作物类型
     */
    private String cropType;

    /**
     * 地块位置
     */
    private String landLocation;

    /**
     * 地块面积（亩）
     */
    private Long landArea;

    /**
     * 病虫害类型（仅喷洒需求有）
     */
    private String pestType;

    /**
     * 巡检目的（仅巡检需求有）
     */
    private String inspectionPurpose;

    /**
     * 期望分辨率（仅巡检需求有）
     */
    private String expectedResolution;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
