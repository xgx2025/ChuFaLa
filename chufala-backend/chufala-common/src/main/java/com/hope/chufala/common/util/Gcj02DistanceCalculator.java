package com.hope.chufala.common.util;


/**
 * GCJ-02 坐标距离计算器。
 *
 * <p>用 Haversine 公式计算球面距离，返回公里数。注意参数顺序为 (经度, 纬度)，
 * 传反会得到无意义的距离（纬度不会超过 ±90°）。
 *
 * @author 谢光湘
 */
public class Gcj02DistanceCalculator {
    // 地球平均半径（米）
    private static final double EARTH_RADIUS = 6371000.0;

    /**
     * 计算两个GCJ-02坐标点之间的距离（单位：公里）
     *
     * @param lon1 点1经度
     * @param lat1 点1纬度
     * @param lon2 点2经度
     * @param lat2 点2纬度
     * @return 距离（km）
     */
    public static Double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        // 将经纬度转换为弧度
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double radLon1 = Math.toRadians(lon1);
        double radLon2 = Math.toRadians(lon2);

        // 差值
        double deltaLat = radLat2 - radLat1;
        double deltaLon = radLon2 - radLon1;

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c /1000;
    }

    // 重载方法：传入数组或对象
    /**
     * 以数组形式计算距离（数组格式为 [经度, 纬度]）。
     *
     * @param point1 点1坐标数组
     * @param point2 点2坐标数组
     * @return 距离（km）
     */
    public static Double calculateDistance(Double[] point1, Double[] point2) {
        return calculateDistance(point1[0], point1[1], point2[0], point2[1]);
    }
}

