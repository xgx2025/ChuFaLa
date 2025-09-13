package com.hope.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 坐标系转换工具类（支持WGS84/GCJ02/BD09互转）
 */
public class CoordinateTransformUtil {
    // 常量优化：X_PI、A、EE保持标准值，PI改用JDK内置Math.PI（精度一致且更规范）
    private static final double X_PI = Math.PI * 3000.0 / 180.0;
    private static final double A = 6378245.0; // 地球长半轴（GCJ02坐标系标准值）
    private static final double EE = 0.00669342162296594323; // 地球偏心率平方


    /**
     * WGS84坐标系 → 腾讯/高德坐标系（GCJ02）
     * @param longitude WGS84经度
     * @param latitude  WGS84纬度
     * @return 转换后的GCJ02坐标（Point对象）
     */
    public static Point wgs84ToGcj02(double longitude, double latitude) {
        validateCoordinate(longitude, latitude); // 先校验经纬度合法性
        if (outOfChina(longitude, latitude)) {
            return new Point(longitude, latitude); // 境外坐标不转换
        }

        double dLat = transformLat(longitude - 105.0, latitude - 35.0);
        double dLon = transformLng(longitude - 105.0, latitude - 35.0);
        double radLat = latitude / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);

        dLat = (dLat * 180.0) / ((A / sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / ((A / (magic * sqrtMagic)) * Math.PI);

        double mgLon = longitude + dLon;
        double mgLat = latitude + dLat;
        return new Point(mgLon, mgLat); // 返回Point，替代原double[]
    }


    /**
     * WGS84坐标系 → 百度坐标系（BD09）
     * @param wgs84Lng WGS84经度
     * @param wgs84Lat WGS84纬度
     * @return 转换后的BD09坐标（Point对象）
     */
    public static Point wgs84ToBd09(double wgs84Lng, double wgs84Lat) {
        Point gcj02 = wgs84ToGcj02(wgs84Lng, wgs84Lat); // 先转GCJ02
        return gcj02ToBd09(gcj02.getLongitude(), gcj02.getLatitude());
    }


    /**
     * 百度坐标系（BD09） → 腾讯/高德坐标系（GCJ02）
     * @param bdLon BD09经度
     * @param bdLat BD09纬度
     * @return 转换后的GCJ02坐标（Point对象）
     */
    public static Point bd09ToGcj02(double bdLon, double bdLat) {
        validateCoordinate(bdLon, bdLat);

        double x = bdLon - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);

        double gcjLng = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        return new Point(gcjLng, gcjLat);
    }


    /**
     * 百度坐标系（BD09） → WGS84坐标系
     * @param bdLon BD09经度
     * @param bdLat BD09纬度
     * @return 转换后的WGS84坐标（Point对象）
     */
    public static Point bd09ToWgs84(double bdLon, double bdLat) {
        Point gcj02 = bd09ToGcj02(bdLon, bdLat); // 先转GCJ02
        return gcj02ToWgs84(gcj02.getLongitude(), gcj02.getLatitude());
    }


    /**
     * 腾讯/高德坐标系（GCJ02） → WGS84坐标系
     * @param longitude GCJ02经度
     * @param latitude  GCJ02纬度
     * @return 转换后的WGS84坐标（Point对象）
     */
    public static Point gcj02ToWgs84(double longitude, double latitude) {
        validateCoordinate(longitude, latitude);
        if (outOfChina(longitude, latitude)) {
            return new Point(longitude, latitude);
        }

        double dLat = transformLat(longitude - 105.0, latitude - 35.0);
        double dLon = transformLng(longitude - 105.0, latitude - 35.0);
        double radLat = latitude / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);

        dLat = (dLat * 180.0) / ((A / sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / ((A / (magic * sqrtMagic)) * Math.PI);

        double mgLon = longitude + dLon;
        double mgLat = latitude + dLat;

        double wgsLng = longitude + (longitude - mgLon);
        double wgsLat = latitude + (latitude - mgLat);
        return new Point(wgsLng, wgsLat);
    }


    /**
     * 腾讯/高德坐标系（GCJ02） → 百度坐标系（BD09）
     * @param gcjLng GCJ02经度
     * @param gcjLat GCJ02纬度
     * @return 转换后的BD09坐标（Point对象）
     */
    public static Point gcj02ToBd09(double gcjLng, double gcjLat) {
        validateCoordinate(gcjLng, gcjLat);

        double x = gcjLng;
        double y = gcjLat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * X_PI);

        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new Point(bdLng, bdLat);
    }


    // ------------------------------ 私有辅助方法 ------------------------------
    /**
     * 计算纬度偏移量（GCJ02加密核心公式）
     */
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 计算经度偏移量（GCJ02加密核心公式）
     */
    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 判断坐标是否在中国大陆境内（境外坐标不做GCJ02/BD09偏移）
     */
    private static boolean outOfChina(double lng, double lat) {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271);
    }

    /**
     * 校验经纬度合法性（经度范围：-180~180，纬度范围：-90~90）
     */
    private static void validateCoordinate(double lng, double lat) {
        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
            throw new IllegalArgumentException(
                    String.format("无效经纬度！经度应在[-180,180]，纬度应在[-90,90]，当前输入：lng=%s, lat=%s", lng, lat)
            );
        }
    }


    /**
     * 坐标点静态内部类（封装经度、纬度，替代原double[]）
     * 用Lombok的@Data自动生成getter/setter/toString等方法，简化代码
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private double longitude; // 经度
        private double latitude;  // 纬度
    }
}