package com.hope.chufala.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hope.chufala.common.exception.InvalidSignatureException;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.common.util.DelayMessageProcessor;
import com.hope.chufala.common.util.RedisWorker;
import com.hope.chufala.common.util.SignaturePriceUtils;
import com.hope.chufala.mq.MultiDelayMessage;
import com.hope.chufala.constant.HotelOrderStatus;
import com.hope.chufala.exception.OrderAlreadyCancelledException;
import com.hope.chufala.model.dto.HotelOrderDTO;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.mapper.HotelMapper;
import com.hope.chufala.mapper.HotelOrderMapper;
import com.hope.chufala.mapper.RoomMapper;
import com.hope.chufala.mapper.RoomDailyStockMapper;
import com.hope.chufala.service.IHotelOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class HotelOrderServiceImpl implements IHotelOrderService {
    @Autowired
    private HotelOrderMapper hotelOrderMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private RoomDailyStockMapper roomDailyStockMapper;
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RedisWorker redisWorker;
    @Autowired
    private SignaturePriceUtils signaturePriceUtils;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public HotelOrder getByOrderId(Long bizId) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", bizId);
        return hotelOrderMapper.selectOne(queryWrapper);
    }

    @Override
    public HotelOrder getByOrderIdAndUserId(Long orderId,Long userId) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId).eq("user_id", userId);
        return hotelOrderMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createHotelOrder(HotelOrderDTO hotelOrderDTO,Long userId) {
        //验证签名
        boolean flag = signaturePriceUtils.verifyPriceSignature(hotelOrderDTO.getRawData(),hotelOrderDTO.getSignature());
        //解析参数
        if (!flag){
           throw new InvalidSignatureException("签名验证失败---"+"用户ID:"+userId);
        }
        Map<String,String> params = signaturePriceUtils.parseParams(hotelOrderDTO.getRawData());
        Long roomTypeId = Long.valueOf(params.get("roomId"));
        Long hotelId = roomMapper.selectHotelIdByRoomTypeId(roomTypeId);
        int roomCount = Integer.parseInt(params.get("roomCount"));
        int nightNum = Integer.parseInt(params.get("nightNum"));
        Double totalPrice = Double.valueOf(params.get("totalPrice"));
        LocalDate checkIn = LocalDate.parse(params.get("checkIn"));
        LocalDate checkOut = LocalDate.parse(params.get("checkOut"));
        long stayNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (roomCount <= 0 || stayNights <= 0 || stayNights > 365
                || stayNights != nightNum || totalPrice <= 0) {
            throw new IllegalArgumentException("订单数量、日期或金额无效");
        }

        // 对 [入住日, 退房日) 每晚分别扣减；事务回滚会恢复已扣减的日期。
        for (LocalDate stayDate = checkIn; stayDate.isBefore(checkOut); stayDate = stayDate.plusDays(1)) {
            roomDailyStockMapper.initializeStock(roomTypeId, stayDate);
            if (roomDailyStockMapper.decreaseStock(roomTypeId, stayDate, roomCount) != 1) {
                throw new IllegalArgumentException("库存不足，日期：" + stayDate);
            }
        }
        //创建订单
        //在此处计算优惠金额或这在签名中计算
        Double discountPrice = 0.0;
        Long orderId = redisWorker.nextId("HotelOrder");
        HotelOrder hotelOrder = new HotelOrder();
        hotelOrder.setOrderId(orderId);
        hotelOrder.setUserId(userId);
        hotelOrder.setOrderStatus(HotelOrderStatus.UNPAID);
        hotelOrder.setBookTime(LocalDateTime.now());
        hotelOrder.setHotelId(hotelId);
        hotelOrder.setRoomTypeId(roomTypeId);
        hotelOrder.setRoomCount(roomCount);
        hotelOrder.setNightNum(nightNum);
        hotelOrder.setTotalPrice(totalPrice);
        hotelOrder.setActualPrice(totalPrice-discountPrice);
        hotelOrder.setDiscount_amount(discountPrice);
        hotelOrder.setCheckIn(checkIn);
        hotelOrder.setCheckOut(checkOut);
        hotelOrder.setGuestName(hotelOrderDTO.getGuestName());
        hotelOrder.setGuestPhone(hotelOrderDTO.getGuestPhone());
        hotelOrder.setGuestEmail(hotelOrderDTO.getGuestEmail());
        hotelOrder.setArrivalTime(hotelOrderDTO.getArrivalTime());
        hotelOrder.setSpecialRequest(hotelOrderDTO.getSpecialRequest());
        String roomName = roomMapper.selectNameById(roomTypeId);
        hotelOrder.setRoomType(roomName);
        hotelOrder.setTitle(roomName+"-"+"-"+nightNum+"晚"+roomCount+"间");
        int row = hotelOrderMapper.insert(hotelOrder);
        if (row != 1){
            throw new RuntimeException("创建订单失败");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    MultiDelayMessage<Long> msg = MultiDelayMessage.of(orderId, 10000L, 10000L, 10000L, 15000L, 15000L, 30000L, 30000L, 60000L, 2 * 60000L, 5 * 60000L, 10 * 60000L, 10 * 60000L);
                    rabbitTemplate.convertAndSend("order.delay.direct", "order.hotel.delay.key", msg, new DelayMessageProcessor(msg.removeNextDelay()));
                } catch (AmqpException e) {
                    log.error("订单延迟消息发送失败，将由定时任务兜底，orderId={}", orderId, e);
                }
            }
        });
        return String.valueOf(orderId);
    }

    @Override
    public void markOrderPaid(Long orderId) {
        UpdateWrapper<HotelOrder> update = new UpdateWrapper<>();
        update.eq("order_id", orderId).eq("order_status", HotelOrderStatus.UNPAID)
                .set("order_status", HotelOrderStatus.PAID).set("paid_time", LocalDateTime.now());
        if (hotelOrderMapper.update(null, update) == 1) {
            return;
        }
        QueryWrapper<HotelOrder> currentOrder = new QueryWrapper<>();
        currentOrder.eq("order_id", orderId).last("FOR UPDATE");
        HotelOrder order = hotelOrderMapper.selectOne(currentOrder);
        if (order != null && HotelOrderStatus.PAID.equals(order.getOrderStatus())) {
            return;
        }
        if (order != null && HotelOrderStatus.CANCELLED.equals(order.getOrderStatus())) {
            throw new OrderAlreadyCancelledException("订单已取消，支付需要退款处理");
        }
        throw new IllegalStateException("订单状态无法更新为已支付，orderId=" + orderId);
    }

    @Override
    public PageResult<HotelOrder> getHotelOrderByUserIdPage(Long userId,String orderStatus,Integer currentPage, Integer pageSize){
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_deleted",0);
        if (orderStatus != null && !"all".equals(orderStatus)){
            queryWrapper.eq("order_status", HotelOrderStatus.normalizeFilter(orderStatus));
        }
        queryWrapper.orderByDesc("book_time");
        IPage<HotelOrder> page = new Page<>(currentPage, pageSize);
        IPage<HotelOrder> hotelOrderIPage = hotelOrderMapper.selectPage(page, queryWrapper);
        PageResult<HotelOrder> pageResult = new PageResult<>();
        List<HotelOrder> hotelOrderList = hotelOrderIPage.getRecords();
        pageResult.setTotal(hotelOrderIPage.getTotal());
        pageResult.setTotalPage((int)hotelOrderIPage.getPages());
        for (HotelOrder hotelOrder : hotelOrderList) {
            Map<String, String> map = hotelMapper.findHotelNameAndAddress(hotelOrder.getHotelId());
            hotelOrder.setHotelName(map.get("name"));
            hotelOrder.setAddress(map.get("address"));
        }
        pageResult.setData(hotelOrderList);
        return pageResult;
    }

    @Override
    public List<HotelOrder> findHotelOrdersByUserIdWithConditions(Long userId, String orderStatus, LocalDate bookTime) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_deleted",0);
        if (orderStatus != null && !"all".equals(orderStatus)) {
            queryWrapper.eq("order_status", HotelOrderStatus.normalizeFilter(orderStatus));
        }
        if (bookTime != null){
            LocalDateTime start = bookTime.atStartOfDay(); // 00:00:00
            LocalDateTime end = bookTime.plusDays(1).atStartOfDay(); // 次日 00:00:00
            queryWrapper.between("book_time", start, end);
        }
        return hotelOrderMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId, Long userId) {
        HotelOrder order = getByOrderIdAndUserId(orderId, userId);
        if (order == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        if (HotelOrderStatus.UNPAID.equals(order.getOrderStatus())) {
            cancelPendingOrder(order, userId, null);
        }
        UpdateWrapper<HotelOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", orderId).eq("user_id", userId)
                .eq("is_deleted", 0).set("is_deleted", 1);
        if (hotelOrderMapper.update(null, updateWrapper) != 1) {
            throw new ResourceNotFoundException("订单不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId).eq("user_id", userId);
        HotelOrder hotelOrder = hotelOrderMapper.selectOne(queryWrapper);
        if (hotelOrder == null){
            throw new ResourceNotFoundException("订单不存在");
        }
        if (HotelOrderStatus.CANCELLED.equals(hotelOrder.getOrderStatus())) {
            return;
        }
        if (HotelOrderStatus.PAID.equals(hotelOrder.getOrderStatus())) {
            throw new IllegalArgumentException("已支付订单不能取消");
        }
        if (!cancelPendingOrder(hotelOrder, userId, null)) {
            HotelOrder current = getByOrderIdAndUserId(orderId, userId);
            if (current != null && HotelOrderStatus.PAID.equals(current.getOrderStatus())) {
                throw new IllegalArgumentException("已支付订单不能取消");
            }
            if (current == null || !HotelOrderStatus.CANCELLED.equals(current.getOrderStatus())) {
                throw new IllegalStateException("订单状态发生变化，请重试");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDelayOrder(Long orderId) {
        HotelOrder order = getByOrderId(orderId);
        if (order != null && HotelOrderStatus.UNPAID.equals(order.getOrderStatus())) {
            cancelPendingOrder(order, null, LocalDateTime.now().minusMinutes(30));
        }
    }

    @Override
    public List<Long> getExpiredUnpaidOrderIds(int limit) {
        QueryWrapper<HotelOrder> query = new QueryWrapper<>();
        query.eq("order_status", HotelOrderStatus.UNPAID)
                .le("book_time", LocalDateTime.now().minusMinutes(30))
                .orderByAsc("book_time").last("LIMIT " + Math.min(Math.max(limit, 1), 100));
        return hotelOrderMapper.selectList(query).stream().map(HotelOrder::getOrderId).toList();
    }

    private boolean cancelPendingOrder(HotelOrder order, Long userId, LocalDateTime expiredBefore) {
        // 老订单可能尚无逐日库存行；在状态变化前初始化，才能正确计入该订单的占用。
        for (LocalDate stayDate = order.getCheckIn(); stayDate.isBefore(order.getCheckOut()); stayDate = stayDate.plusDays(1)) {
            roomDailyStockMapper.initializeStock(order.getRoomTypeId(), stayDate);
        }
        UpdateWrapper<HotelOrder> update = new UpdateWrapper<>();
        update.eq("order_id", order.getOrderId()).eq("order_status", HotelOrderStatus.UNPAID)
                .eq(userId != null, "user_id", userId)
                .le(expiredBefore != null, "book_time", expiredBefore)
                .set("order_status", HotelOrderStatus.CANCELLED);
        if (hotelOrderMapper.update(null, update) != 1) {
            return false;
        }
        for (LocalDate stayDate = order.getCheckIn(); stayDate.isBefore(order.getCheckOut()); stayDate = stayDate.plusDays(1)) {
            if (roomDailyStockMapper.increaseStock(order.getRoomTypeId(), stayDate, order.getRoomCount()) != 1) {
                throw new IllegalStateException("恢复房间库存失败，orderId=" + order.getOrderId()
                        + ", stayDate=" + stayDate);
            }
        }
        return true;
    }

}
