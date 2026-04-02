package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.Banner;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 轮播图Mapper接口
 */
public interface BannerMapper extends BaseMapper<Banner> {
    
    /**
     * 根据用户类型获取启用的轮播图列表
     * @param type 用户类型：farmer(农户) 或 flyer(飞手)
     * @return 轮播图列表
     */
    List<Banner> selectByTypeAndStatus(@Param("type") String type);
    
    /**
     * 获取所有轮播图（管理员使用）
     * @return 轮播图列表
     */
    List<Banner> selectAllBanners();
}