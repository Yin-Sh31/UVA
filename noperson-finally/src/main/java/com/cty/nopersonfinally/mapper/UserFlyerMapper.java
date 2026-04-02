package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFlyerMapper extends BaseMapper<UserFlyer> {

    // 批量查询飞手（支持DemandMatchServiceimpl中的selectBatchIds）
    List<UserFlyer> selectBatchIds(List<Long> ids);

    // 根据用户ID查询飞手信息
    UserFlyer selectByUserId(Long userId);

    // 分页查询符合条件的飞手（用于筛选）
    IPage<UserFlyer> selectPageByCondition(IPage<UserFlyer> page,
                                           @Param(Constants.WRAPPER) Wrapper<UserFlyer> queryWrapper);

    // 根据技能等级和审核状态查询飞手
    List<UserFlyer> selectBySkillAndAudit(@Param("skillLevel") String skillLevel,
                                          @Param("auditStatus") Integer auditStatus);
}