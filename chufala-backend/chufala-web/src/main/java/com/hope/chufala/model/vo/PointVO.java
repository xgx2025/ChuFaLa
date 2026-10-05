package com.hope.chufala.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 坐标点 VO，由 Mapper 查询映射，用于返回单个经纬度坐标。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class PointVO {
    /** 经度 */
    private Double longitude;
    /** 纬度 */
    private Double latitude;
}
