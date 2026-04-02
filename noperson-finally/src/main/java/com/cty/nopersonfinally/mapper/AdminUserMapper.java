package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员用户Mapper接口
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    
    /**
     * 根据手机号查询管理员用户
     * @param phone 手机号
     * @return 管理员用户实体
     */
    AdminUser selectByPhone(String phone);
    
    /**
     * 根据用户名查询管理员用户
     * @param username 用户名
     * @return 管理员用户实体
     */
    AdminUser selectByUsername(String username);
}