package com.cty.nopersonfinally.service.impl;

import com.cty.nopersonfinally.mapper.SystemConfigMapper;
import com.cty.nopersonfinally.pojo.entity.SystemConfig;
import com.cty.nopersonfinally.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public String getConfigValue(String configKey) {
        return systemConfigMapper.getConfigValueByKey(configKey);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean updateConfigValue(String configKey, String configValue) {
        try {
            // 尝试更新配置
            int result = systemConfigMapper.updateConfigValueByKey(configKey, configValue);
            
            // 如果更新失败，说明配置不存在，创建新配置
            if (result == 0) {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                config.setConfigDesc("系统自动创建");
                config.setCreateTime(LocalDateTime.now());
                config.setUpdateTime(LocalDateTime.now());
                systemConfigMapper.insert(config);
            }
            
            return true;
        } catch (Exception e) {
            log.error("更新配置失败 - key: {}, value: {}", configKey, configValue, e);
            return false;
        }
    }

    @Override
    public boolean isFlyerOrderBanned() {
        String value = getConfigValue(FLYER_ORDER_BAN_KEY, "false");
        return Boolean.parseBoolean(value);
    }

    @Override
    public boolean setFlyerOrderBan(boolean banned) {
        boolean result = updateConfigValue(FLYER_ORDER_BAN_KEY, String.valueOf(banned));
        if (result) {
            log.info("飞手接单状态已更新: {}", banned ? "禁止接单" : "允许接单");
        }
        return result;
    }
}