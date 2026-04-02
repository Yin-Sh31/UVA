package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.dto.DeviceMaintainDTO;
import com.cty.nopersonfinally.pojo.entity.DeviceMaintainRecord;
import com.cty.nopersonfinally.pojo.vo.DeviceMaintainVO;

public interface DeviceMaintainService extends IService<DeviceMaintainRecord> {
    // 创建维护记录
    Long createMaintainRecord(DeviceMaintainDTO dto, Long operatorId);

    // 审核维护记录
    boolean auditMaintainRecord(Long recordId, Integer status, Long adminId);

    // 分页查询设备维护记录
    Page<DeviceMaintainVO> getDeviceMaintainRecords(Long deviceId, int pageNum, int pageSize);

    // 查询设备最近一次维护记录
    DeviceMaintainVO getLastMaintainRecord(Long deviceId);

    // 检查设备是否需要维护（超过30天未维护）
    boolean checkNeedMaintain(Long deviceId);
}