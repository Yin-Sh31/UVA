package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.mapper.FarmerMapper;
import com.cty.nopersonfinally.pojo.vo.FarmerInfoVO;
import com.cty.nopersonfinally.service.FarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 农户服务实现类
 */
@Service
public class FarmerServiceImpl implements FarmerService {

    @Autowired
    private FarmerMapper farmerMapper;
    
    @Override
    public Page<FarmerInfoVO> getFarmersForFlyer(int pageNum, int pageSize) {
        // 创建分页对象
        Page<FarmerInfoVO> page = new Page<>(pageNum, pageSize);
        
        // 调用Mapper层方法查询农户列表
        return farmerMapper.selectFarmersForFlyer(page);
    }
    
    @Override
    public Page<FarmerInfoVO> getFarmersForAdmin(int pageNum, int pageSize, String keyword) {
        // 创建分页对象
        Page<FarmerInfoVO> page = new Page<>(pageNum, pageSize);
        
        // 调用Mapper层方法查询农户列表（带关键词搜索）
        return farmerMapper.selectFarmersForAdmin(page, keyword);
    }
    
    @Override
    public FarmerInfoVO getFarmerInfoById(Long farmerId) {
        // 调用Mapper层方法根据ID查询农户信息
        return farmerMapper.selectFarmerInfoById(farmerId);
    }
}