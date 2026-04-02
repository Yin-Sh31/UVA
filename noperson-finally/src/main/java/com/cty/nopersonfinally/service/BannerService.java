package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.entity.Banner;
import java.util.List;

/**
 * 轮播图Service接口
 */
public interface BannerService {
    
    /**
     * 根据用户类型获取轮播图列表
     * @param type 用户类型：farmer(农户) 或 flyer(飞手)
     * @return 轮播图列表
     */
    List<Banner> getBannersByType(String type);
    
    /**
     * 获取所有轮播图（管理员使用）
     * @return 轮播图列表
     */
    List<Banner> getAllBanners();
    
    /**
     * 创建轮播图
     * @param banner 轮播图对象
     * @return 创建的轮播图对象
     */
    Banner createBanner(Banner banner);
    
    /**
     * 更新轮播图
     * @param id 轮播图ID
     * @param banner 轮播图对象
     * @return 更新后的轮播图对象
     */
    Banner updateBanner(Long id, Banner banner);
    
    /**
     * 删除轮播图
     * @param id 轮播图ID
     */
    void deleteBanner(Long id);
}