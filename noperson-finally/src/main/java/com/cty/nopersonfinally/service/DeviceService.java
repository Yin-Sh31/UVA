package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.pojo.enums.DeviceStatusEnum;

import java.util.List;
import java.util.Map;

/**
 * 设备管理服务接口
 */
import com.cty.nopersonfinally.pojo.vo.DeviceDetailVO;

public interface DeviceService extends IService<DroneDevice> {


    boolean checkDeviceAvailable(Long deviceId, Long flyerId);

    /**
     * 更新设备状态
     * @param deviceId 设备ID
     * @param status 目标状态
     * @param operatorId 操作人ID
     * @return 是否更新成功
     */
    boolean updateDeviceStatus(Long deviceId, DeviceStatusEnum status, Long operatorId);

    /**
     * 获取指定飞手的所有设备
     * @param flyerId 飞手ID
     * @return 设备列表
     */
    List<DroneDevice> getDevicesByFlyerId(Long flyerId);

    /**
     * 获取指定状态的设备列表
     * @param status 设备状态
     * @return 设备列表
     */
    List<DroneDevice> getDevicesByStatus(DeviceStatusEnum status);
    
    /**
     * 获取所有可用设备（状态正常且未被租借）
     * @return 可用设备列表
     */
    List<DroneDevice> getAvailableDevices();

    /**
     * 检查设备是否可用于任务
     * @param deviceId 设备ID
     * @return 检查结果
     */
    boolean checkDeviceAvailable(Long deviceId);

    /**
     * 绑定设备到飞手
     *
     * @param deviceId 设备ID
     * @param flyerId  飞手ID
     * @return 是否绑定成功
     */
    int bindDeviceToFlyer(Long deviceId, Long flyerId);

    /**
     * 解除设备与飞手的绑定
     * @param deviceId 设备ID
     * @return 是否解绑成功
     */
    boolean unbindDeviceFromFlyer(Long deviceId);
    
    /**
     * 解除设备与飞手的绑定（带操作类型）
     * @param deviceId 设备ID
     * @param isCancel 是否为取消租借操作，true表示取消租借(状态3)，false表示归还操作(状态2)
     * @return 是否解绑成功
     */
    boolean unbindDeviceFromFlyer(Long deviceId, boolean isCancel);
    
    /**
     * 解除设备与飞手的绑定（支持指定rentalId）
     * @param deviceId 设备ID
     * @param rentalId 租借记录ID，指定要更新的特定租借记录
     * @param isCancel 是否为取消租借操作，true表示取消租借(状态3)，false表示归还操作(状态2)
     * @return 是否解绑成功
     */
    boolean unbindDeviceFromFlyer(Long deviceId, Long rentalId, boolean isCancel);
    
    /**
     * 租借无人机
     * @param deviceId 设备ID
     * @param flyerId 飞手ID
     * @return 是否租借成功
     */
    boolean rentDevice(Long deviceId, Long flyerId);
    
    /**
     * 归还无人机
     * @param deviceId 设备ID
     * @param flyerId 飞手ID
     * @return 是否归还成功
     */
    boolean returnDevice(Long deviceId, Long flyerId);
    
    /**
     * 查询单个无人机设备详情
     * @param deviceId 设备ID
     * @return 设备详情VO
     */
    DeviceDetailVO getDeviceDetail(Long deviceId);

    /**
     * 获取所有无人机列表（管理员）（无论何状态）
     * @param pageNum
     * @param pageSize
     * @param ownerId
     * @param status
     * @return
     */
    IPage<DeviceDetailVO> getAllDevices(int pageNum, int pageSize, Long ownerId, Integer status);

    /**
     * 审核无人机资质
     * @param deviceId 设备ID
     * @param isQualified 是否合格（1-合格，0-不合格）
     * @param operatorId 操作人ID
     * @return 是否审核成功
     */
    boolean auditDeviceQualification(Long deviceId, Integer isQualified, Long operatorId);

    /**
     * 获取待审核的无人机列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 待审核无人机列表
     */
    IPage<DroneDevice> getPendingAuditDevices(int pageNum, int pageSize);
    
    /**
     * 获取待审核的无人机列表（带机主ID筛选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param ownerId 机主ID（可选）
     * @return 待审核无人机详情列表
     */
    IPage<DeviceDetailVO> getDevicesForAudit(int pageNum, int pageSize, Long ownerId);
    
    /**
     * 审核无人机资质
     * @param deviceId 设备ID
     * @param status 审核结果：1-通过 2-拒绝
     * @param adminId 审核人ID
     * @param remark 审核备注
     * @return 是否审核成功
     */
    boolean auditDevice(Long deviceId, Integer status, Long adminId, String remark);

    boolean deleteDevice(Long deviceId);

    boolean OwnerUpdateDeviceById(DroneDevice device);
    
    /**
     * 获取飞手租借历史记录
     * @param flyerId 飞手ID
     * @return 租借历史记录列表
     */
    List<Map<String, Object>> getFlyerRentalHistory(Long flyerId);
}
