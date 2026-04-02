package com.cty.nopersonfinally.service.impl;

import com.cty.nopersonfinally.pojo.dto.UserInfoUpdateDTO;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.entity.TransactionRecord;
import com.cty.nopersonfinally.pojo.vo.UserInfoVO;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import com.cty.nopersonfinally.service.TransactionRecordService;
import com.cty.nopersonfinally.service.UserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * 用户信息服务实现类
 * 实现用户个人信息的获取和修改功能
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private TransactionRecordService transactionRecordService;

    /**
     * 根据用户ID获取用户个人信息
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        // 查询系统用户表获取用户基本信息
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 转换为VO对象返回
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(user.getUserId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setRoleType(user.getRoleType());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setCreateTime(user.getCreateTime());
        userInfoVO.setLastLoginTime(user.getLastLoginTime());
        userInfoVO.setBalance(user.getBalance());
        userInfoVO.setAddress(user.getAddress());
        
        return userInfoVO;
    }

    /**
     * 更新用户个人信息
     */
    @Override
    @Transactional
    public boolean updateUserInfo(UserInfoUpdateDTO dto, Long userId) {
        // 查询用户是否存在
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 更新可修改字段
        if (dto.getRealName() != null && !dto.getRealName().isEmpty()) {
            user.setRealName(dto.getRealName());
        }
        
        if (dto.getAvatar() != null && !dto.getAvatar().isEmpty()) {
            user.setAvatar(dto.getAvatar());
        }
        
        // 更新地址信息
        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
            user.setAddress(dto.getAddress());
        }
        
        // 执行更新操作
        int rows = sysUserMapper.updateById(user);
        return rows > 0;
    }
    
    /**
     * 查询用户余额
     */
    @Override
    public Double getUserBalance(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getBalance() != null ? user.getBalance() : 0.0;
    }
    
    /**
     * 修改用户余额
     */
    @Override
    @Transactional
    public boolean updateUserBalance(Long userId, Double amount) {
        // 验证参数
        if (amount == null) {
            throw new RuntimeException("金额不能为空");
        }
        
        // 查询用户是否存在
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 计算新余额
        Double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        Double newBalance = currentBalance + amount;
        
        // 防止余额为负数
        if (newBalance < 0) {
            throw new RuntimeException("余额不足");
        }
        
        // 更新余额
        user.setBalance(newBalance);
        int rows = sysUserMapper.updateById(user);
        
        // 添加交易记录
        if (rows > 0) {
            if (amount > 0) {
                // 余额增加，添加收入记录
                transactionRecordService.addIncomeRecord(userId, amount, "余额更新收入", null);
            } else if (amount < 0) {
                // 余额减少，添加支出记录
                transactionRecordService.addExpenseRecord(userId, Math.abs(amount), "余额更新支出", null);
            }
        }
        
        return rows > 0;
    }
    
    /**
     * 充值金额到用户账户
     */
    @Override
    @Transactional
    public boolean rechargeBalance(Long userId, Double amount) {
        // 验证参数
        if (amount == null || amount <= 0) {
            throw new RuntimeException("充值金额必须为正数");
        }
        
        // 查询用户是否存在
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 计算新余额
        Double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        Double newBalance = currentBalance + amount;
        
        // 更新余额
        user.setBalance(newBalance);
        int rows = sysUserMapper.updateById(user);
        
        // 添加交易记录：充值收入
        if (rows > 0) {
            transactionRecordService.addIncomeRecord(userId, amount, "账户充值", null);
        }
        
        return rows > 0;
    }
    
    /**
     * 提现用户余额
     */
    @Override
    @Transactional
    public boolean withdrawBalance(Long userId, Double amount) {
        // 验证参数
        if (amount == null || amount <= 0) {
            throw new RuntimeException("提现金额必须为正数");
        }
        
        // 查询用户是否存在
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 计算新余额
        Double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        Double newBalance = currentBalance - amount;
        
        // 检查余额是否充足
        if (newBalance < 0) {
            throw new RuntimeException("余额不足，无法提现");
        }
        
        // 更新余额
        user.setBalance(newBalance);
        int rows = sysUserMapper.updateById(user);
        
        // 添加交易记录：提现支出
        if (rows > 0) {
            transactionRecordService.addExpenseRecord(userId, amount, "账户提现", null);
        }
        
        return rows > 0;
    }
    
    /**
     * 查询用户收支明细
     * 注意：此实现为基础版本，实际项目中需要创建交易记录表和对应的Mapper
     */
    @Override
    public Page<?> getUserTransactions(Long userId, Integer page, Integer pageSize, 
                                     LocalDateTime startTime, LocalDateTime endTime, 
                                     String transactionType) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        // 验证交易类型参数
        if (transactionType != null && !transactionType.equals("INCOME") && !transactionType.equals("EXPENSE")) {
            throw new RuntimeException("交易类型参数无效，只能是'INCOME'或'EXPENSE'");
        }
        
        // 检查用户是否存在
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 调用交易记录服务层查询
        return transactionRecordService.getUserTransactions(userId, page, pageSize, startTime, endTime, transactionType);
    }
}