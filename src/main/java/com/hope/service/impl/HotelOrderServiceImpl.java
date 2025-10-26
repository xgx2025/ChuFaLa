package com.hope.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hope.constant.ResultCode;
import com.hope.domain.dto.HotelOrderDTO;
import com.hope.domain.entity.HotelOrder;
import com.hope.domain.vo.PageResult;
import com.hope.domain.vo.Result;
import com.hope.mapper.HotelMapper;
import com.hope.mapper.HotelOrderMapper;
import com.hope.mapper.RoomTypeMapper;
import com.hope.service.IHotelOrderService;
import com.hope.utils.RedisWorker;
import com.hope.utils.SignaturePriceUtil;
import com.hope.utils.SimpleRedisLock;
import com.hope.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class HotelOrderServiceImpl implements IHotelOrderService {
    @Autowired
    private HotelOrderMapper hotelOrderMapper;
    @Autowired
    private RoomTypeMapper roomTypeMapper;
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RedisWorker redisWorker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public HotelOrder getByOrderId(Long orderId) {
        QueryWrapper<HotelOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return hotelOrderMapper.selectOne(queryWrapper);
    }

    @Override
    public Result createHotelOrder(HotelOrderDTO hotelOrderDTO) {
        //验证签名
        boolean flag = SignaturePriceUtil.verifyPriceSignature(hotelOrderDTO.getRawData(),hotelOrderDTO.getSignature());
        //解析参数
        if (!flag){
            return Result.fail(ResultCode.DATA_NOT_SAFE);
        }
        Map<String,String> params = SignaturePriceUtil.parseParams(hotelOrderDTO.getRawData());
        Long roomTypeId = Long.valueOf(params.get("roomId"));
        Long hotelId = roomTypeMapper.selectHotelIdByRoomTypeId(roomTypeId);
        int roomCount = Integer.parseInt(params.get("roomCount"));
        int nightNum = Integer.parseInt(params.get("nightNum"));
        Double totalPrice = Double.valueOf(params.get("totalPrice"));
        LocalDate checkIn = LocalDate.parse(params.get("checkIn"));
        LocalDate checkOut = LocalDate.parse(params.get("checkOut"));
        Claims claims = ThreadLocalUtil.get();
        Long userId = claims.get("userId", Long.class);

        //修改库存
        SimpleRedisLock redisLock = new SimpleRedisLock("roomType:"+roomTypeId,stringRedisTemplate);
        boolean success = false;
        try{
            if (!redisLock.tryLock(100)){
                return Result.fail(ResultCode.SYSTEM_BUSY);
            }
             success = roomTypeMapper.updateStock(roomTypeId,roomCount);
        }finally {
            redisLock.unlock();
        }
        //创建订单
        if (!success){
            return Result.fail(ResultCode.SYSTEM_BUSY);
        }
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
        String roomName =roomTypeMapper.selectNameById(roomTypeId);
        hotelOrder.setRoomType(roomName);
        hotelOrder.setTitle(roomName+"-"+"-"+nightNum+"晚"+roomCount+"间");
        int row = hotelOrderMapper.insert(hotelOrder);
        if (row != 1){
            return Result.fail(ResultCode.SYSTEM_BUSY);
        }
        return Result.ok(String.valueOf(orderId));
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
    public PageResult<HotelOrder> queryAllHotelOrder(Long userId,String orderStatus,Integer currentPage, Integer pageSize){
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
    public boolean deleteOrder(Long orderId) {
        UpdateWrapper<HotelOrder> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("order_id", orderId).set("is_deleted",1);
        int row = hotelOrderMapper.update(null, queryWrapper);
        return row>0;
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        UpdateWrapper<HotelOrder> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("order_id", orderId).set("order_status","已取消");
        int row = hotelOrderMapper.update(null, queryWrapper);
        return row>0;
    }


}
