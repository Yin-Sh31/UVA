package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;
import com.cty.nopersonfinally.pojo.vo.MatchResultVO;
import java.util.List;

public interface DemandMatchService {
    /**
     * 匹配喷洒需求与飞手+设备
     * @param dto 喷洒需求DTO
     * @return 匹配结果列表（Top10）
     */
    List<MatchResultVO> matchSprayDemand(OrderDemandDTO dto);
}