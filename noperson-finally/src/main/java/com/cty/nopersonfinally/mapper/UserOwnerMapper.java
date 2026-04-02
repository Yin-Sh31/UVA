package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.UserOwner;
import com.cty.nopersonfinally.pojo.vo.UserOwnerBasicVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerDetailVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerVO;
import org.apache.ibatis.annotations.Param;

/**
 * 机主信息Mapper
 */
public interface UserOwnerMapper extends BaseMapper<UserOwner> {

    /**
     * 分页查询机主列表（带VO转换）
     */
    IPage<UserOwnerVO> selectOwnerPage(Page<UserOwner> page, @Param(Constants.WRAPPER) Wrapper<UserOwner> queryWrapper);

    /**
     * 根据用户ID查询机主信息（关联用户表）
     */
    UserOwnerDetailVO selectByUserId(@Param("userId") Long userId);
}