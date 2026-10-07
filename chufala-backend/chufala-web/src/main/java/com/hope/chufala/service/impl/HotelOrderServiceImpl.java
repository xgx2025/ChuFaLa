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
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.mapper.RoomMapper;
import com.hope.chufala.mapper.RoomDailyStockMapper;
import com.hope.chufala.service.IHotelOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 酒店订单服务实现。
 *
 * <p>下单核心链路是「验签 → 逐日扣库存 → 落库 → 事务提交后发延迟消息」：
 * 价格由服务端签名保证不可篡改；库存按 [入住日, 退房日) 逐晚 CAS 扣减，
 * 任一晚不足即抛异常，由事务回滚已扣的日期。取消与超时回收统一走
 * {@link #cancelPendingOrder}，用 CAS（UNPAID → CANCELLED）保证库存只回补一次。
 *
 * @author 谢光湘
 */
@Slf4j
@Service
public class HotelOrderServiceImpl implements IHotelOrderService {
    private static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Shanghai");
    @Autowired
    private HotelOrderMapper hotelOrderMapper;
    @Autowired
    private PayRecordMapper payRecordMapper;
    @Autowired
    private HotelPaymentCloseGuard hotelPaymentCloseGuard;
    @Autowired
    private PlatformTransactionManager transactionManager;
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

    /**
     * 按业务订单号查询订单。
     *
     * @param bizId 业务订单 ID
     * @return 订单，不存在返回 null
     */
    @Override
    public HotelOrder getByOrderId(Long bizId) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", bizId);
        return hotelOrderMapper.selectOne(queryWrapper);
    }

    /**
     * 按业务订单号与用户 ID 查询订单（用于归属校验）。
     *
     * @param orderId 业务订单 ID
     * @param userId  用户 ID
     * @return 订单，不匹配返回 null
     */
    @Override
    public HotelOrder getByOrderIdAndUserId(Long orderId,Long userId) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId).eq("user_id", userId);
        return hotelOrderMapper.selectOne(queryWrapper);
    }

    /**
     * 创建酒店订单。
     *
     * <p>签名校验不通过抛 InvalidSignatureException；日期、间夜数或金额不合法抛
     * IllegalArgumentException；库存不足时在扣减处失败并回滚。订单号由 RedisWorker
     * 生成；延迟消息用于 30 分钟后回收未支付订单，发送失败由定时任务兜底。
     *
     * @param hotelOrderDTO 下单参数（含被签名的原始数据）
     * @param userId        下单用户 ID
     * @return 新订单的业务订单号
     */
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
        hotelOrder.setBookTime(LocalDateTime.now(HOTEL_ZONE));
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

    /**
     * 将订单标记为已支付（CAS：仅 UNPAID 可推进）。
     *
     * <p>更新失败时回查订单：已支付视为幂等成功，已取消则抛
     * OrderAlreadyCancelledException 触发退款流程。
     *
     * @param orderId 业务订单 ID
     */
    @Override
    public void markOrderPaid(Long orderId) {
        UpdateWrapper<HotelOrder> update = new UpdateWrapper<>();
        update.eq("order_id", orderId).eq("order_status", HotelOrderStatus.UNPAID)
                .set("order_status", HotelOrderStatus.PAID).set("paid_time", LocalDateTime.now(HOTEL_ZONE));
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

    /**
     * 分页查询用户的酒店订单，并补全酒店名称与地址。
     *
     * @param userId      用户 ID
     * @param orderStatus 订单状态筛选，"all" 或 null 表示不过滤
     * @param currentPage 当前页
     * @param pageSize    每页大小
     * @return 分页结果
     */
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

    /**
     * 按条件查询用户的全部订单（供智能助手工具调用）。
     *
     * @param userId      用户 ID
     * @param orderStatus 订单状态筛选，"all" 或 null 表示不过滤
     * @param bookTime    下单日期筛选，按自然日区间匹配，可为空
     * @return 订单列表
     */
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

    /**
     * 删除订单（逻辑删除）；未支付订单会先取消以回补库存。
     *
     * @param orderId 业务订单 ID
     * @param userId  用户 ID
     */
    @Override
    public void deleteOrder(Long orderId, Long userId) {
        HotelOrder order = getByOrderIdAndUserId(orderId, userId);
        if (order == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        HotelPaymentCloseGuard.Result proof = HotelOrderStatus.UNPAID.equals(order.getOrderStatus())
                ? hotelPaymentCloseGuard.prepareCancellation(orderId, paymentDeadline(order)) : null;
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            HotelOrder current = lockedOrder(orderId);
            if (current == null || !userId.equals(current.getUserId())) {
                throw new ResourceNotFoundException("订单不存在");
            }
            if (HotelOrderStatus.UNPAID.equals(current.getOrderStatus())) {
                requireSafeCancellation(orderId, proof);
                cancelPendingOrder(current, userId, null);
            }
            UpdateWrapper<HotelOrder> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("order_id", orderId).eq("user_id", userId)
                    .eq("is_deleted", 0).set("is_deleted", 1);
            if (hotelOrderMapper.update(null, updateWrapper) != 1) {
                throw new ResourceNotFoundException("订单不存在");
            }
        });
    }

    /**
     * 取消订单（用户主动取消，回补库存）。
     *
     * <p>已取消视为幂等直接返回；已支付不允许取消。CAS 失败时会回查订单状态，
     * 以区分「已被并发支付」与「状态已变化」两种情况。
     *
     * @param orderId 业务订单 ID
     * @param userId  用户 ID
     */
    @Override
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
        HotelPaymentCloseGuard.Result proof = hotelPaymentCloseGuard.prepareCancellation(
                orderId, paymentDeadline(hotelOrder));
        if (proof == HotelPaymentCloseGuard.Result.PAID) {
            throw new IllegalArgumentException("订单已付款，不能取消");
        }
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            HotelOrder current = lockedOrder(orderId);
            if (current == null || !userId.equals(current.getUserId())) {
                throw new ResourceNotFoundException("订单不存在");
            }
            if (HotelOrderStatus.PAID.equals(current.getOrderStatus())) {
                throw new IllegalArgumentException("已支付订单不能取消");
            }
            if (HotelOrderStatus.CANCELLED.equals(current.getOrderStatus())) {
                return;
            }
            requireSafeCancellation(orderId, proof);
            if (!cancelPendingOrder(current, userId, null)) {
                throw new IllegalStateException("订单状态发生变化，请重试");
            }
        });
    }

    /**
     * 取消超时未支付订单（延迟消息触发，回补库存）。
     *
     * <p>额外要求下单时间早于 30 分钟前，避免延迟消息提前到达时误取消新订单。
     *
     * @param orderId 业务订单 ID
     */
    @Override
    public void cancelDelayOrder(Long orderId) {
        HotelOrder order = getByOrderId(orderId);
        if (order == null || !HotelOrderStatus.UNPAID.equals(order.getOrderStatus())
                || LocalDateTime.now(HOTEL_ZONE).isBefore(paymentDeadline(order))) {
            return;
        }
        HotelPaymentCloseGuard.Result proof = hotelPaymentCloseGuard.prepareCancellation(
                orderId, paymentDeadline(order));
        if (proof == HotelPaymentCloseGuard.Result.PAID) {
            return;
        }
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            HotelOrder current = lockedOrder(orderId);
            if (current == null || !HotelOrderStatus.UNPAID.equals(current.getOrderStatus())
                    || LocalDateTime.now(HOTEL_ZONE).isBefore(paymentDeadline(current))) {
                return;
            }
            requireSafeCancellation(orderId, proof);
            cancelPendingOrder(current, null, LocalDateTime.now(HOTEL_ZONE).minusMinutes(30));
        });
    }

    private LocalDateTime paymentDeadline(HotelOrder order) {
        if (order.getBookTime() == null) {
            throw new IllegalStateException("酒店订单缺少下单时间，orderId=" + order.getOrderId());
        }
        return order.getBookTime().plusMinutes(30);
    }

    private HotelOrder lockedOrder(Long orderId) {
        QueryWrapper<HotelOrder> query = new QueryWrapper<>();
        query.eq("order_id", orderId).last("FOR UPDATE");
        return hotelOrderMapper.selectOne(query);
    }

    private void requireSafeCancellation(Long orderId, HotelPaymentCloseGuard.Result proof) {
        if (proof == null || proof == HotelPaymentCloseGuard.Result.PAID) {
            throw new IllegalStateException("订单支付状态尚未确认，暂不释放库存，orderId=" + orderId);
        }
        // 发起支付也先锁酒店订单，再创建支付记录。锁内重查可覆盖取消前的竞态。
        if (proof == HotelPaymentCloseGuard.Result.NO_PAYMENT_RECORD
                && payRecordMapper.selectByOrderId(orderId) != null) {
            throw new IllegalStateException("订单刚发起支付，请重试取消，orderId=" + orderId);
        }
    }

    /**
     * 查询超时未支付的订单 ID 列表（供定时任务兜底扫描）。
     *
     * @param limit 单次最大返回条数，内部钳制在 1~100
     * @param afterOrderId 上次扫描的订单号；null 表示从头开始
     * @return 订单 ID 列表
     */
    @Override
    public List<Long> getExpiredUnpaidOrderIds(int limit, Long afterOrderId) {
        QueryWrapper<HotelOrder> query = new QueryWrapper<>();
        query.eq("order_status", HotelOrderStatus.UNPAID)
                .le("book_time", LocalDateTime.now(HOTEL_ZONE).minusMinutes(30))
                .gt(afterOrderId != null, "order_id", afterOrderId)
                .orderByAsc("order_id").last("LIMIT " + Math.min(Math.max(limit, 1), 100));
        return hotelOrderMapper.selectList(query).stream().map(HotelOrder::getOrderId).toList();
    }

    /**
     * 取消未支付订单并回补逐日库存（幂等）。
     *
     * <p>先用 CAS 把 UNPAID 改为 CANCELLED，只有成功的那次才回补库存，
     * 因此并发取消/支付只会生效一次。userId 与 expiredBefore 为可选约束：
     * 用户主动取消传 userId，超时回收传 expiredBefore。
     *
     * @param order         订单
     * @param userId        限定归属用户，可为 null
     * @param expiredBefore 限定下单时间上限，可为 null
     * @return 本次是否真正完成了取消
     */
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
