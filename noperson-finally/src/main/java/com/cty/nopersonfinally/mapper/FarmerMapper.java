package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.vo.FarmerInfoVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 农户Mapper接口
 * 提供查询农户信息的数据库操作方法
 */
@Mapper
public interface FarmerMapper {
    
    /**
     * 飞手分页查询农户列表
     * @param page 分页对象
     * @return 农户信息分页结果
     */
    Page<FarmerInfoVO> selectFarmersForFlyer(Page<FarmerInfoVO> page);
    
    /**
     * 管理员分页查询农户列表（支持关键词搜索）
     * @param page 分页对象
     * @param keyword 搜索关键词（用户名、手机号、真实姓名）
     * @return 农户信息分页结果
     */
    Page<FarmerInfoVO> selectFarmersForAdmin(Page<FarmerInfoVO> page, String keyword);
    
    /**
     * 根据ID查询农户详细信息
     * @param farmerId 农户ID
     * @return 农户详细信息
     */
    FarmerInfoVO selectFarmerInfoById(Long farmerId);
}