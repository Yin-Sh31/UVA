package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.AdminUserMapper;
import com.cty.nopersonfinally.pojo.entity.AdminUser;
import com.cty.nopersonfinally.service.AdminUserService;
import com.cty.nopersonfinally.utils.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 管理员用户服务实现类
 */
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public AdminUser getByPhone(String phone) {
        return baseMapper.selectByPhone(phone);
    }
    
    @Override
    public AdminUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }
    
    @Override
    public boolean createAdmin(AdminUser adminUser) {
        // 检查用户名是否已存在
        if (getByUsername(adminUser.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (adminUser.getPhone() != null && !adminUser.getPhone().isEmpty()) {
            if (getByPhone(adminUser.getPhone()) != null) {
                throw new BusinessException("手机号已存在");
            }
        }
        
        // 加密密码
        adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        
        // 设置默认状态
        if (adminUser.getStatus() == null) {
            adminUser.setStatus(1);
        }
        
        return save(adminUser);
    }
    
    @Override
    public boolean updateStatus(Long id, Integer status) {
        AdminUser admin = getById(id);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        
        admin.setStatus(status);
        return updateById(admin);
    }
    
    @Override
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        AdminUser admin = getById(id);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        
        // 验证旧密码
        boolean passwordMatches = false;
        if (admin.getPassword().startsWith("$2a$")) {
            // BCrypt加密密码
            passwordMatches = passwordEncoder.matches(oldPassword, admin.getPassword());
        } else {
            // 明文密码
            passwordMatches = oldPassword.equals(admin.getPassword());
        }
        
        if (!passwordMatches) {
            throw new BusinessException("旧密码错误");
        }
        
        // 设置新密码
        admin.setPassword(passwordEncoder.encode(newPassword));
        return updateById(admin);
    }
}