package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.entity.SystemConfig;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {
    
    /**
     * 飞手接单控制配置键名
     */
    String FLYER_ORDER_BAN_KEY = "flyer_order_ban";
    
    /**
     * 获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 获取配置值，带默认值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 更新配置值
     */
    boolean updateConfigValue(String configKey, String configValue);
    
    /**
     * 获取飞手接单状态
     */
    boolean isFlyerOrderBanned();
    
    /**
     * 设置飞手接单状态
     */
    boolean setFlyerOrderBan(boolean banned);
}