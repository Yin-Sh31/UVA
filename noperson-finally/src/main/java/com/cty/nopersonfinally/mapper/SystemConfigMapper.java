package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.SystemConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper接口
 */
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键名获取配置值
     */
    String getConfigValueByKey(@Param("configKey") String configKey);
    
    /**
     * 更新配置值
     */
    int updateConfigValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);
}