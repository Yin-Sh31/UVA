package com.cty.nopersonfinally.service.impl;

import com.cty.nopersonfinally.mapper.DroneDeviceMapper;
import com.cty.nopersonfinally.mapper.FlyerMapper;
import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;
import com.cty.nopersonfinally.pojo.entity.DroneDevice;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.pojo.enums.DeviceStatusEnum;
import com.cty.nopersonfinally.pojo.vo.MatchResultVO;
import com.cty.nopersonfinally.service.DemandMatchService;
import com.cty.nopersonfinally.utils.GeoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandMatchServiceimpl implements DemandMatchService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private FlyerMapper flyerMapper;
    @Autowired
    private DroneDeviceMapper deviceMapper;

    // 智能匹配：根据喷洒需求匹配飞手+设备
    public List<MatchResultVO> matchSprayDemand(OrderDemandDTO dto) {
        // 1. 从Redis获取“在线飞手”和“空闲设备”（减少DB查询）
        List<Long> onlineFlyerIds = stringRedisTemplate.opsForList().range("online:flyers", 0, -1)
                .stream()
                .map(Long::parseLong)  // String转换为Long
                .collect(Collectors.toList());
        List<Long> idleDeviceIds = stringRedisTemplate.opsForList().range("idle:devices", 0, -1)
                .stream()
                .map(Long::parseLong)  // String转换为Long
                .collect(Collectors.toList());

        // 2. 筛选条件1：飞手具备“喷洒认证”+ 设备为“大载重喷洒机型”
        List<UserFlyer> qualifiedFlyers = flyerMapper.selectBatchIds(onlineFlyerIds).stream()
                .filter(f -> "喷洒认证".equals(f.getSkillLevel()))
                .filter(f -> 1 == f.getAuditStatus()) // 审核通过
                .collect(Collectors.toList());
        List<DroneDevice> qualifiedDevices = deviceMapper.selectBatchIds(idleDeviceIds).stream()
                .filter(d -> "喷洒".equals(d.getDeviceType()))
                .filter(d -> d.getMaxLoad() >= 10) // 大载重（≥10kg）
                .collect(Collectors.toList());

        // 3. 多维度加权计算匹配得分（参考文档中匹配维度）
        List<MatchResultVO> matchResults = new ArrayList<>();
        for (UserFlyer flyer : qualifiedFlyers) {
            for (DroneDevice device : qualifiedDevices) {
                // 计算单维度得分
                int distanceScore = calculateDistanceScore(flyer.getLocation(), dto.getLandLocation()); // 距离得分（0-30）
                int reputationScore = calculateReputationScore(flyer.getReputation()); // 信誉得分（0-20）
                int priceScore = calculatePriceScore(flyer.getPricePerAcre(), dto.getBudget()); // 价格得分（0-10）
                int availabilityScore = calculateAvailabilityScore(flyer.getIsFree(), String.valueOf(device.getStatus())); // 可用性得分（0-15）

                // 总得分 = 距离(30%) + 类型匹配(25固定分) + 信誉(20%) + 价格(10%) + 可用性(15%)
                int totalScore = distanceScore + 25 + reputationScore + priceScore + availabilityScore;

                // 构建匹配结果
                MatchResultVO result = new MatchResultVO();
                result.setFlyerId(flyer.getUserId());
                result.setFlyerName(flyer.getUserName());
                result.setDeviceId(device.getDeviceId());
                result.setDeviceModel(device.getModel());
                result.setTotalScore(totalScore);
                result.setEstimatedPrice(calculateEstimatedPrice(flyer, device, dto.getLandArea())); // 预估价格
                matchResults.add(result);
            }
        }

        // 4. 按总得分降序排序，返回Top10匹配结果
        return matchResults.stream()
                .sorted((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()))
                .limit(10)
                .collect(Collectors.toList());
    }


    // 辅助方法：计算价格匹配得分（0-10分，价格越接近预算得分越高）
    private int calculatePriceScore(Double flyerPrice, Double budget) {
        if (flyerPrice == null || budget == null || budget <= 0) return 0;
        // 价格超出预算20%以上不得分
        if (flyerPrice > budget * 1.2) return 0;
        // 价格低于预算10%以内得满分
        if (flyerPrice >= budget * 0.9) return 10;
        // 按比例计算得分（线性衰减）
        double ratio = flyerPrice / budget;
        return (int) Math.round(ratio * 10);
    }

    // 辅助方法：计算可用性得分（0-15分，飞手空闲且设备可用得满分）
    // 统一飞手空闲状态参数为Boolean（与UserFlyer的isFree字段匹配）
    private int calculateAvailabilityScore(Boolean isFlyerFree, String deviceStatus) {
        int score = 0;
        // 飞手空闲得8分
        if (Boolean.TRUE.equals(isFlyerFree)) score += 8;
        // 设备状态为"NORMAL"得7分（与DeviceStatusEnum.NORMAL的code匹配）
        if (Objects.equals(String.valueOf(DeviceStatusEnum.NORMAL.getCode()), deviceStatus)) score += 7;
        return score;
    }


    // 预估价格计算
    private Double calculateEstimatedPrice(UserFlyer flyer, DroneDevice device, Double landArea) {
        if (flyer.getPricePerAcre() == null || landArea == null) return 0.0;
        // 基础价格 = 每亩单价 * 面积
        double basePrice = flyer.getPricePerAcre() * landArea;
        // 设备损耗费 = 基础价格 * 5%
        double deviceFee = basePrice * 0.05;
        // 保留两位小数
        return Math.round((basePrice + deviceFee) * 100) / 100.0;
    }

    // 补充距离得分计算（示例实现）
    private int calculateDistanceScore(String flyerLocation, String landLocation) {
        // 解析经纬度（假设格式"lat,lon"）
        String[] flyerCoords = flyerLocation.split(",");
        String[] landCoords = landLocation.split(",");
        double lat1 = Double.parseDouble(flyerCoords[0]);
        double lon1 = Double.parseDouble(flyerCoords[1]);
        double lat2 = Double.parseDouble(landCoords[0]);
        double lon2 = Double.parseDouble(landCoords[1]);

        // 计算距离（公里）
        double distance = GeoUtil.calculateDistance(lat1, lon1, lat2, lon2);

        // 距离越近得分越高（0-30分）
        if (distance <= 5) return 30;
        if (distance <= 10) return 25;
        if (distance <= 20) return 20;
        if (distance <= 50) return 10;
        return 0;
    }

    // 补充信誉得分计算
    private int calculateReputationScore(double reputation) {
        // 信誉分范围0-100，转换为0-20分
        return (int) Math.min(20, reputation / 5);
    }



    // 补充可用性得分计算
    private int calculateAvailabilityScore(Integer flyerFree, String deviceStatus) {
        // 飞手空闲（1）且设备空闲（IDLE）得满分
        if (flyerFree == 1 && "IDLE".equals(deviceStatus)) return 15;
        if (flyerFree == 1 || "IDLE".equals(deviceStatus)) return 5;
        return 0;
    }


}