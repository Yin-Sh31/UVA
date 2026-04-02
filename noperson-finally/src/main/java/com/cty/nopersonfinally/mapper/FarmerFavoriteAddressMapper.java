package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.FarmerFavoriteAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 农户常用地址Mapper接口
 */
@Mapper
public interface FarmerFavoriteAddressMapper extends BaseMapper<FarmerFavoriteAddress> {
    // 继承BaseMapper后，自动获得CRUD方法
}
