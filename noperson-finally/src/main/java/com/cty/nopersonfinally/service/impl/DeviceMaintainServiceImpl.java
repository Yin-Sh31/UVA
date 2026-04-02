package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cty.nopersonfinally.mapper.DeviceMaintainRecordMapper;
import com.cty.nopersonfinally.mapper.DroneDeviceMapper;
import com.cty.nopersonfinally.pojo.dto.DeviceMaintainDTO;
import com.cty.nopersonfinally.pojo.entity.DeviceMaintainRecord;
import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.pojo.vo.DeviceMaintainVO;
import com.cty.nopersonfinally.service.DeviceMaintainService;
import com.cty.nopersonfinally.service.NotificationService;
import com.cty.nopersonfinally.service.TransactionRecordService;
import com.cty.nopersonfinally.utils.BusinessException;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DeviceMaintainServiceImpl extends ServiceImpl<DeviceMaintainRecordMapper, DeviceMaintainRecord> implements DeviceMaintainService {

    @Autowired
    private DeviceMaintainRecordMapper deviceMaintainRecordMapper;

    @Autowired
    private DroneDeviceMapper deviceMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionRecordService transactionRecordService;

    @Override
    @Transactional
    public Long createMaintainRecord(DeviceMaintainDTO dto, Long operatorId) {
        // 校验设备是否存在
        DroneDevice device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        DeviceMaintainRecord record = new DeviceMaintainRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setOperatorId(operatorId);
        record.setMaintainType(dto.getMaintainType());
        record.setFaultDescription(dto.getFaultDescription());
        record.setMaintainContent(dto.getMaintainContent());
        record.setCost(dto.getCost());
        record.setReplaceParts(dto.getReplaceParts());
        record.setMaintainTime(LocalDateTime.now());
        record.setStatus(0); // 初始状态：待审核

        deviceMaintainRecordMapper.insert(record);
        
        // 如果有维护费用，添加交易记录（设备所有者支出）
        if (record.getCost() != null && record.getCost() > 0) {
            // 获取设备信息，找到设备所有者
            if (device != null && device.getOwnerId() != null) {
                String maintainTypeDesc = dto.getMaintainType() == 1 ? "常规保养" : "故障维修";
                transactionRecordService.addExpenseRecord(
                    device.getOwnerId(),
                    record.getCost(),
                    "设备维护费用 - " + maintainTypeDesc,
                    record.getRecordId()
                );
            }
        }
        
        return record.getRecordId();
    }

    @Override
    @Transactional
    public boolean auditMaintainRecord(Long recordId, Integer status, Long adminId) {
        DeviceMaintainRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("维护记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("记录已审核，无法重复操作");
        }

        // 更新记录状态
        record.setStatus(status);
        record.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(record);

        // 审核通过时，更新设备最后维护时间
        if (success && status == 1) {
            DroneDevice device = new DroneDevice();
            device.setDeviceId(record.getDeviceId());
            device.setLastMaintainTime(record.getMaintainTime());
            deviceMapper.updateById(device);
        }

        return success;
    }

    @Override
    public Page<DeviceMaintainVO> getDeviceMaintainRecords(Long deviceId, int pageNum, int pageSize) {
        Page<DeviceMaintainRecord> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DeviceMaintainRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId)
                .orderByDesc("maintain_time");

        Page<DeviceMaintainRecord> recordPage = deviceMaintainRecordMapper.selectPage(page, queryWrapper);
        return (Page<DeviceMaintainVO>) recordPage.convert(this::convertToVO);
    }

    @Override
    public DeviceMaintainVO getLastMaintainRecord(Long deviceId) {
        QueryWrapper<DeviceMaintainRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId)
                .eq("status", 1) // 只查已审核的
                .orderByDesc("maintain_time")
                .last("limit 1");

        DeviceMaintainRecord record = deviceMaintainRecordMapper.selectOne(queryWrapper);
        return record != null ? convertToVO(record) : null;
    }

    @Override
    public boolean checkNeedMaintain(Long deviceId) {
        DeviceMaintainVO lastRecord = getLastMaintainRecord(deviceId);
        if (lastRecord == null) {
            return true; // 从未维护过，需要维护
        }

        // 计算距上次维护已过天数
        long days = ChronoUnit.DAYS.between(
                lastRecord.getMaintainTime(),
                LocalDateTime.now()
        );
        return days >= 30; // 超过30天需要维护
    }

    private DeviceMaintainVO convertToVO(DeviceMaintainRecord record) {
        DeviceMaintainVO vo = new DeviceMaintainVO();
        vo.setRecordId(record.getRecordId());
        vo.setDeviceId(record.getDeviceId());
        vo.setMaintainType(record.getMaintainType());
        vo.setMaintainTypeDesc(record.getMaintainType() == 1 ? "常规保养" : "故障维修");
        vo.setFaultDescription(record.getFaultDescription());
        vo.setMaintainContent(record.getMaintainContent());
        vo.setCost(record.getCost());
        vo.setMaintainTime(record.getMaintainTime());
        vo.setStatus(record.getStatus());
        vo.setStatusDesc(record.getStatus() == 0 ? "待审核" : "已确认");
        return vo;
    }
}