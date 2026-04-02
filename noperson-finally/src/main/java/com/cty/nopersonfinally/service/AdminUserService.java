package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.entity.AdminUser;

/**
 * 管理员用户服务接口
 */
public interface AdminUserService extends IService<AdminUser> {
    
    /**
     * 根据手机号查询管理员
     * @param phone 手机号
     * @return 管理员实体
     */
    AdminUser getByPhone(String phone);
    
    /**
     * 根据用户名查询管理员
     * @param username 用户名
     * @return 管理员实体
     */
    AdminUser getByUsername(String username);
    
    /**
     * 创建管理员用户
     * @param adminUser 管理员实体
     * @return 是否创建成功
     */
    boolean createAdmin(AdminUser adminUser);
    
    /**
     * 更新管理员状态
     * @param id 管理员ID
     * @param status 状态：0-禁用 1-正常
     * @return 是否更新成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 修改管理员密码
     * @param id 管理员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);
}