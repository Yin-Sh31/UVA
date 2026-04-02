package com.cty.nopersonfinally.utils;


/**
 * 地理距离计算工具类
 * 用于计算两个经纬度点之间的直线距离
 */
public class GeoUtil {

    private static final double EARTH_RADIUS = 6371.0; // 地球半径，单位：公里

    /**
     * 将角度转换为弧度
     */
    private static double toRadians(double degree) {
        return degree * Math.PI / 180.0;
    }

    /**
     * 计算两个经纬度点之间的距离（单位：公里）
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 两点之间的距离（公里）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        double radLat1 = toRadians(lat1);
        double radLon1 = toRadians(lon1);
        double radLat2 = toRadians(lat2);
        double radLon2 = toRadians(lon2);

        // 计算纬度差和经度差
        double deltaLat = radLat2 - radLat1;
        double deltaLon = radLon2 - radLon1;

        // 应用Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离（公里）
        return EARTH_RADIUS * c;
    }
}
