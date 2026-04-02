package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.dto.AuditDTO;
import com.cty.nopersonfinally.pojo.dto.UserOwnerDTO;
import com.cty.nopersonfinally.pojo.entity.UserOwner;
import com.cty.nopersonfinally.pojo.vo.UserOwnerDetailVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerDeviceStatVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerVO;

/**
 * 机主服务接口
 */
public interface UserOwnerService extends IService<UserOwner> {

    /**
     * 根据用户ID查询机主信息
     * @param userId 用户ID
     * @return 机主实体
     */
    UserOwner getByUserId(Long userId);

    /**
     * 提交机主资质资料（新提交或重新提交）
     * @param dto 机主资料DTO
     * @param userId 当前登录用户ID
     */
    void submitQualification(UserOwnerDTO dto, Long userId);

    /**
     * 机主审核处理（管理员操作）
     * @param dto 审核DTO
     */
    void auditQualification(AuditDTO dto);

    /**
     * 根据用户ID查询机主详情VO（包含关联信息）
     *
     * @param userId 用户ID
     * @return 机主详情VO
     */
    UserOwnerDetailVO getDetailByUserId(Long userId);

    /**
     * 分页查询机主列表（带条件）
     *
     * @param pageNum     页码
     * @param pageSize    页大小
     * @param area        区域筛选（可选）
     * @param auditStatus 审核状态（可选）
     * @return 分页VO列表
     */
    IPage<UserOwnerVO> getOwnerPage(int pageNum, int pageSize, String area, Integer auditStatus);

    /**
     * 更新机主设备数量（总数和可用数）
     * @param ownerId 机主ID
     * @param totalChange 总数变更量（正数增加，负数减少）
     * @param availableChange 可用数变更量
     */
    void updateDeviceCounts(Long ownerId, int totalChange, int availableChange);

    /**
     * 获取机主设备统计信息
     * @param ownerId 机主ID
     * @return 设备统计VO
     */
    UserOwnerDeviceStatVO getDeviceStat(Long ownerId);

    /**
     * 更新机主常用区域
     * @param commonArea 新常用区域
     * @param userId 当前用户ID
     */
    void updateCommonArea(String commonArea, Long userId);
}