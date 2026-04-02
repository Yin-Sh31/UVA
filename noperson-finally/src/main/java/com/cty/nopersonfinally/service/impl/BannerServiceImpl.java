package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cty.nopersonfinally.mapper.BannerMapper;
import com.cty.nopersonfinally.pojo.entity.Banner;
import com.cty.nopersonfinally.service.BannerService;
import com.cty.nopersonfinally.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图Service实现类
 */
@Service
public class BannerServiceImpl implements BannerService {
    
    @Autowired
    private BannerMapper bannerMapper;
    
    @Override
    public List<Banner> getBannersByType(String type) {
        // 参数校验
        if (!"farmer".equals(type) && !"flyer".equals(type)) {
            throw new BusinessException("用户类型无效，请传入farmer或flyer");
        }
        // 查询指定类型且状态为启用的轮播图
        return bannerMapper.selectByTypeAndStatus(type);
    }
    
    @Override
    public List<Banner> getAllBanners() {
        // 查询所有轮播图
        return bannerMapper.selectAllBanners();
    }
    
    @Override
    public Banner createBanner(Banner banner) {
        // 参数校验
        validateBanner(banner);
        // 设置默认排序值
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        // 设置默认状态为启用
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        // 保存轮播图
        bannerMapper.insert(banner);
        return banner;
    }
    
    @Override
    public Banner updateBanner(Long id, Banner banner) {
        // 检查轮播图是否存在
        Banner existingBanner = bannerMapper.selectById(id);
        if (existingBanner == null) {
            throw new BusinessException("轮播图不存在");
        }
        
        // 参数校验
        validateBanner(banner);
        
        // 设置ID
        banner.setId(id);
        
        // 更新轮播图
        bannerMapper.updateById(banner);
        return bannerMapper.selectById(id);
    }
    
    @Override
    public void deleteBanner(Long id) {
        // 检查轮播图是否存在
        Banner existingBanner = bannerMapper.selectById(id);
        if (existingBanner == null) {
            throw new BusinessException("轮播图不存在");
        }
        
        // 删除轮播图
        bannerMapper.deleteById(id);
    }
    
    /**
     * 验证轮播图参数
     */
    private void validateBanner(Banner banner) {
        if (banner.getTitle() == null || banner.getTitle().trim().isEmpty()) {
            throw new BusinessException("轮播图标题不能为空");
        }
        if (banner.getImageUrl() == null || banner.getImageUrl().trim().isEmpty()) {
            throw new BusinessException("轮播图图片URL不能为空");
        }
        if (banner.getType() == null || (!"farmer".equals(banner.getType()) && !"flyer".equals(banner.getType()))) {
            throw new BusinessException("用户类型无效，请传入farmer或flyer");
        }
        // 移除对targetUrl的非空验证，允许为空，仅用于展示
    }
}