package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 酒店下单请求参数。
 *
 * <p>rawData 与 signature 成对出现：rawData 是订单原始数据（含房型、价格、日期等），
 * signature 是其签名，下单时由 SignaturePriceUtils 校验，防止前端篡改价格。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelOrderDTO {
    /** 订单原始数据（被签名的内容） */
    private String rawData;
    /** 价格签名，用于校验 rawData 未被篡改 */
    private String signature;
    /** 入住人姓名 */
    private String guestName;
    /** 入住人手机号 */
    private String guestPhone;
    /** 入住人邮箱 */
    private String guestEmail;
    /** 预计到店时间 */
    private String arrivalTime;
    /** 特殊要求 */
    private String specialRequest;
}
