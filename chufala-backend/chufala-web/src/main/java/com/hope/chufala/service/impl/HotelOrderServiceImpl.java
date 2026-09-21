package com.hope.chufala.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hope.chufala.common.exception.InvalidSignatureException;
import com.hope.chufala.common.util.DelayMessageProcessor;
import com.hope.chufala.common.util.RedisWorker;
import com.hope.chufala.common.util.SignaturePriceUtils;
import com.hope.chufala.common.util.SimpleRedisLock;
import com.hope.chufala.mq.MultiDelayMessage;
import com.hope.chufala.model.dto.HotelOrderDTO;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.model.entity.Room;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.mapper.HotelMapper;
import com.hope.chufala.mapper.HotelOrderMapper;
import com.hope.chufala.mapper.RoomMapper;
import com.hope.chufala.service.IHotelOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private HotelMapper hotelMapper;
    @Autowired
    private RedisWorker redisWorker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
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

        //扣减库存（与建单同处一个事务，任一失败一起回滚）
        SimpleRedisLock redisLock = new SimpleRedisLock("roomType:"+roomTypeId,stringRedisTemplate);
        // 100 是锁的过期时间（秒），不是等待时长：拿不到锁立即失败，避免请求线程被长时间阻塞
        if (!redisLock.tryLock(100)){
            throw new RuntimeException("获取锁失败");
        }
        //锁必须在事务提交/回滚之后再释放。
        //若在事务内提前释放，其他线程可立即拿到锁并读到尚未提交的旧库存。
        registerUnlockAfterCompletion(redisLock);
        boolean success = roomMapper.updateStock(roomTypeId,roomCount);
        if (!success){
           throw new RuntimeException("库存不足");
        }
        //创建订单
        //在此处计算优惠金额或这在签名中计算
        Double discountPrice = 0.0;
        Long orderId = redisWorker.nextId("HotelOrder");
        HotelOrder hotelOrder = new HotelOrder();
        hotelOrder.setOrderId(orderId);
        hotelOrder.setUserId(userId);
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
        try {
            //延迟检测订单状态
            MultiDelayMessage<Long> msg = MultiDelayMessage.of(orderId, 10000L, 10000L, 10000L, 15000L, 15000L, 30000L, 30000L, 60000L, 2 * 60000L, 5 * 60000L, 10 * 60000L, 10 * 60000L); //延迟时间30分钟
            rabbitTemplate.convertAndSend("order.delay.direct", "order.hotel.delay.key", msg, new DelayMessageProcessor(msg.removeNextDelay()));
            log.info("延迟消息发送成功！");
        }catch (AmqpException e){
            log.error("延迟消息发送异常！",e);
        }
        return String.valueOf(orderId);
    }

    /**
     * 在当前事务完成后（提交或回滚）释放 Redis 锁。
     * afterCompletion 回调与事务在同一线程执行，锁的线程归属校验仍然成立。
     */
    private void registerUnlockAfterCompletion(SimpleRedisLock lock) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                lock.unlock();
            }
        });
    }

    @Override
    public void updateStatus(Long orderId, String orderStatus) {
       HotelOrder order = new HotelOrder();
       order.setId(orderId);
       order.setOrderStatus(orderStatus);
       order.setPaidTime(LocalDateTime.now());
       QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
       queryWrapper.eq("order_id", orderId);
       hotelOrderMapper.update(order, queryWrapper);
    }

    @Override
    public PageResult<HotelOrder> getHotelOrderByUserIdPage(Long userId,String orderStatus,Integer currentPage, Integer pageSize){
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("is_deleted",0);
        if (!"all".equals(orderStatus)){
            queryWrapper.eq("order_status", orderStatus);
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
        queryWrapper.eq("user_id", userId).eq("is_deleted",0).eq("order_status", orderStatus);
        if (bookTime != null){
            LocalDateTime start = bookTime.atStartOfDay(); // 00:00:00
            LocalDateTime end = bookTime.plusDays(1).atStartOfDay(); // 次日 00:00:00
            queryWrapper.between("book_time", start, end);
        }
        return hotelOrderMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteOrder(Long orderId) {
        try {
            UpdateWrapper<HotelOrder> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("order_id", orderId).set("is_deleted", 1);
            hotelOrderMapper.update(null, updateWrapper);
        }catch (Exception e){
           throw new RuntimeException(e);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        HotelOrder hotelOrder = hotelOrderMapper.selectOne(queryWrapper);
        if (hotelOrder == null){
            throw new RuntimeException("订单不存在!");
        }
        //修改订单状态
        UpdateWrapper<HotelOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("order_id", orderId).set("order_status","已取消");
        hotelOrderMapper.update(null, updateWrapper);
        //恢复库存
        UpdateWrapper<Room> roomUpdateWrapper = new UpdateWrapper<>();
        roomUpdateWrapper.eq("id", hotelOrder.getRoomTypeId()).setSql("stock = stock + " + hotelOrder.getRoomCount());
//        roomUpdateWrapper.eq("id", hotelOrder.getRoomTypeId()).set("stock", "stock + " + hotelOrder.getRoomCount());
        roomMapper.update(null, roomUpdateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelDelayOrder(Long orderId,Long roomId,int roomCount) {
        //修改订单状态
        UpdateWrapper<HotelOrder> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("order_id", orderId).set("order_status","已取消");
        hotelOrderMapper.update(null, queryWrapper);
        //恢复库存
        UpdateWrapper<Room> roomUpdateWrapper = new UpdateWrapper<>();
        roomUpdateWrapper.eq("id", roomId).setSql("stock = stock + " + roomCount);
//        roomUpdateWrapper.eq("id", roomId).set("stock","stock + "+roomCount);
        roomMapper.update(null, roomUpdateWrapper);
    }


}
