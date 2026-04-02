package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FlyerMapper extends BaseMapper<UserFlyer> {
    // 批量查询飞手（支持DemandMatchServiceimpl中的selectBatchIds）
    List<UserFlyer> selectBatchIds(List<Long> ids);

    // 根据用户ID查询飞手信息
    UserFlyer selectByUserId(Long userId);
}