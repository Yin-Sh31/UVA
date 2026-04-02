package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;

public interface DemandService {


    void publishSprayDemand(OrderDemandDTO dto, Long farmerId);
}
