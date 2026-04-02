package com.cty.nopersonfinally.service.impl;


import com.cty.nopersonfinally.mapper.DemandMapper;
import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;
import com.cty.nopersonfinally.pojo.entity.OrderDemand;
import com.cty.nopersonfinally.pojo.enums.DemandStatusEnum;
import com.cty.nopersonfinally.service.DemandService;
import com.cty.nopersonfinally.utils.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DemandServiceimpl implements DemandService {

    @Autowired
    private DemandMapper demandMapper;

    // 发布喷洒需求
    @Transactional
    @Override
    public void publishSprayDemand(OrderDemandDTO dto, Long farmerId) {
        // 参数校验
        if (dto == null) {
            throw new BusinessException("喷洒需求信息不能为空");
        }
        if (farmerId == null || farmerId <= 0) {
            throw new BusinessException("农户ID无效");
        }
        if (dto.getLandBoundary() == null || dto.getLandBoundary().trim().isEmpty()) {
            throw new BusinessException("地块边界不能为空");
        }
        if (dto.getExpectedTime() == null) {
            throw new BusinessException("期望时间不能为空");
        }
        if (dto.getBudget() == null || dto.getBudget() <= 0) {
            throw new BusinessException("预算必须大于0");
        }

        // 转换DTO为实体
        OrderDemand demand = new OrderDemand();
        demand.setFarmerId(farmerId);
        // 不再设置reportId
        demand.setLandBoundary(dto.getLandBoundary());
        demand.setCropType(dto.getCropType());
        demand.setPestType(dto.getPestType());
        demand.setLandLocation(dto.getLandLocation());
        demand.setLandArea(dto.getLandArea());
        demand.setExpectedTime(dto.getExpectedTime());
        demand.setBudget(dto.getBudget());
        demand.setStatus(Integer.valueOf(DemandStatusEnum.PENDING.getCode())); // 替换硬编码
        demand.setCreateTime(LocalDateTime.now());

        demandMapper.insert(demand);
    }
}