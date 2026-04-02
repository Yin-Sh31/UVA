package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.dto.UserInfoUpdateDTO;
import com.cty.nopersonfinally.pojo.vo.UserInfoVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;

/**
 * 用户信息服务接口
 * 提供用户个人信息的获取和修改功能
 */
public interface UserInfoService {

    /**
     * 根据用户ID获取用户个人信息
     * @param userId 用户ID
     * @return 用户信息VO
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户个人信息
     * @param dto 用户信息更新DTO
     * @param userId 用户ID
     * @return 是否更新成功
     */
    boolean updateUserInfo(UserInfoUpdateDTO dto, Long userId);
    
    /**
     * 查询用户余额
     * @param userId 用户ID
     * @return 用户余额
     */
    Double getUserBalance(Long userId);
    
    /**
     * 修改用户余额
     * @param userId 用户ID
     * @param amount 金额（正数增加，负数减少）
     * @return 是否修改成功
     */
    boolean updateUserBalance(Long userId, Double amount);
    
    /**
     * 充值金额到用户账户
     * @param userId 用户ID
     * @param amount 充值金额（必须为正数）
     * @return 是否充值成功
     */
    boolean rechargeBalance(Long userId, Double amount);
    
    /**
     * 提现用户余额
     * @param userId 用户ID
     * @param amount 提现金额（必须为正数）
     * @return 是否提现成功
     */
    boolean withdrawBalance(Long userId, Double amount);
    
    /**
     * 查询用户收支明细
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param transactionType 交易类型(in/out)
     * @return 分页结果
     */
    Page<?> getUserTransactions(Long userId, Integer page, Integer pageSize, 
                              LocalDateTime startTime, LocalDateTime endTime, 
                              String transactionType);
}