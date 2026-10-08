package com.hope.chufala.service;

import com.hope.chufala.model.dto.HotelOrderDTO;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.common.model.vo.PageResult;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;

/**
 * 酒店订单服务。
 *
 * <p>覆盖下单、支付确认、取消与超时回收全流程；库存的扣减与回补依赖
 * room_daily_stock 的逐日记录，取消操作按幂等语义只归还一次。
 *
 * @author 谢光湘
 */
public interface IHotelOrderService {
    /**
     * 按业务订单号查询订单。
     *
     * @param bizId 业务订单 ID
     * @return 订单，不存在返回 null
     */
    HotelOrder getByOrderId(Long bizId);

    /**
     * 按业务订单号与用户 ID 查询未删除的订单（用于归属校验）。
     *
     * @param orderId 业务订单 ID
     * @param userId  用户 ID
     * @return 订单；不存在、不属于该用户或已删除时为 null
     */
    HotelOrder getByOrderIdAndUserId(Long orderId,Long userId);

    /**
     * 创建酒店订单（校验价格签名并占用库存）。
     *
     * @param hotelOrderDTO 下单参数（含被签名的原始数据）
     * @param userId        下单用户 ID
     * @return 新订单的业务订单号
     */
    String createHotelOrder(HotelOrderDTO hotelOrderDTO,Long userId);

    /**
     * 将订单标记为已支付。
     *
     * @param orderId 业务订单 ID
     */
    void markOrderPaid(Long orderId);

    /**
     * 按下单时间降序、ID 降序游标分页查询用户未删除的酒店订单。
     *
     * @param userId      用户 ID
     * @param orderStatus 订单状态筛选；null 或 all 表示全部
     * @param size        每页大小，最大为 50
     * @param cursor      上一页返回的 nextCursor；首页为空
     * @return 分页结果；nextCursor 仅有下一页时返回
     */
    PageResult<HotelOrder> getHotelOrderByUserIdPage(Long userId, String orderStatus, Integer size, String cursor);

    /**
     * 按业务订单号查询用户未删除的订单，并补全酒店名称与地址。
     *
     * @param userId 用户 ID
     * @param orderId 业务订单号
     * @return 订单；不存在或不属于该用户时为 null
     */
    HotelOrder getHotelOrderDetail(Long userId, Long orderId);

    /**
     * 按条件查询用户的全部订单。
     *
     * @param userId      用户 ID
     * @param orderStatus 订单状态筛选，可为空
     * @param bookTime    下单时间筛选，可为空
     * @return 订单列表
     */
    List<HotelOrder> findHotelOrdersByUserIdWithConditions(Long userId, String orderStatus, @Nullable LocalDate bookTime);
    /**
     * 删除订单（校验归属）。
     *
     * @param orderId 业务订单 ID
     * @param userId  用户 ID
     */
    void deleteOrder(Long orderId, Long userId);

    /**
     * 取消订单（用户主动取消，回补库存）。
     *
     * @param orderId 业务订单 ID
     * @param userId  用户 ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 取消超时未支付订单（延迟消息触发，回补库存）。
     *
     * @param orderId 业务订单 ID
     */
    void cancelDelayOrder(Long orderId);

    /**
     * 查询超时未支付的订单 ID 列表。
     *
     * @param limit 单次最大返回条数
     * @param afterOrderId 上次扫描的订单号；null 表示从头开始
     * @return 订单 ID 列表
     */
    List<Long> getExpiredUnpaidOrderIds(int limit, Long afterOrderId);
}
