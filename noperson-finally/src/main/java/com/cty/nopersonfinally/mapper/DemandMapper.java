package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.OrderDemand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandMapper extends BaseMapper<OrderDemand> {
    @Insert("insert into demand (farmer_id, flyer_id, report_id, device_id, land_id, land_name, land_boundary, crop_type, pest_type, inspection_purpose, expected_resolution, land_location, land_area, expected_time, budget, status, order_type, accept_time, start_time, complete_time, cancel_time, cancel_reason, create_time, update_time, payment_amount, payment_status, payment_time, payment_method, payment_record_id)" +
            " values " +
            "(#{farmerId}, #{flyerId}, #{reportId}, #{deviceId}, #{landId}, #{landName}, #{landBoundary}, #{cropType}, #{pestType}, #{inspectionPurpose}, #{expectedResolution}, #{landLocation}, #{landArea}, #{expectedTime}, #{budget}, #{status}, #{orderType}, #{acceptTime}, #{startTime}, #{completeTime}, #{cancelTime}, #{cancelReason}, #{createTime}, #{updateTime}, #{paymentAmount}, #{paymentStatus}, #{paymentTime}, #{paymentMethod}, #{paymentRecordId})")
    void insertOrder(OrderDemand demand);
    // 继承BaseMapper后，自动获得CRUD方法
}