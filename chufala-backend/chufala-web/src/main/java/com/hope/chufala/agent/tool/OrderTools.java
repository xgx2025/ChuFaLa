package com.hope.chufala.agent.tool;

import cn.hutool.system.UserInfo;
import com.hope.chufala.model.dto.HotelOrderDTO;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.model.entity.User;
import com.hope.chufala.service.IAiConversationService;
import com.hope.chufala.service.IHotelOrderService;
import com.hope.chufala.service.IRoomService;
import com.hope.chufala.service.IUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@Component
public class OrderTools {
    @Autowired
    private IAiConversationService aiConversationService;
    @Autowired
    private IHotelOrderService hotelOrderService;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private IUserService userService;



    @Tool(description = "查询当前用户的酒店订单。可按状态筛选：'未支付'、'已支付'、'已取消'，如果用户未提状态，就查所有订单。可按时间筛选，如果用户没有指明时间，就将时间设置null，不用询问用户时间")
    public String queryHotelOrderByUserIdPage(@ToolParam(description = "当前会话ID") Long conversationId,@ToolParam(description = "订单状态,状态有‘未支付’、‘已支付’、‘已取消’") String orderStatus, @ToolParam(description = "下单时间") LocalDate bookTime) {
        Long currentUserId = aiConversationService.getUserIdByConversationId(conversationId);
        List<HotelOrder> orderPageResult = hotelOrderService.findHotelOrdersByUserIdWithConditions(currentUserId,orderStatus,bookTime);
       return orderPageResult.toString();
    }

    @Tool(description = "通过订单ID查询酒店订单详情")
    public String queryHotelOrderByOrderId(@ToolParam(description = "当前会话ID") Long conversationId,@ToolParam(description = "订单ID") Long orderId) {
        Long currentUserId = aiConversationService.getUserIdByConversationId(conversationId);
        HotelOrder hotelOrder = hotelOrderService.getByOrderIdAndUserId(orderId,currentUserId);
        if (hotelOrder == null){
            return "未找到该订单";
        }
        return hotelOrder.toString();
    }

    @Tool(description = "创建酒店订单")
    public String createHotelOrder(@ToolParam(description = "当前会话ID") Long conversationId,@ToolParam(description = "入住时间") LocalDate checkIn, @ToolParam(description = "退房时间（当晚可住）") LocalDate checkOut, @ToolParam(description = "预定房间数量") Integer roomCount, @ToolParam(description = "房型ID") Long roomTypeId, @ToolParam(description = "预计到店时间，格式如14:00") String arrivalTime) {
        HotelOrderDTO hotelOrderDTO = new HotelOrderDTO(null,null,null,null,null,checkIn.toString(),null);
        Long currentUserId = aiConversationService.getUserIdByConversationId(conversationId);
        User user= userService.getUserById(currentUserId);
        hotelOrderDTO.setGuestEmail(user.getEmail());
        hotelOrderDTO.setGuestName(user.getUsername());
        hotelOrderDTO.setGuestPhone(user.getPhone());
        hotelOrderDTO.setArrivalTime(arrivalTime);
        //计算价格
        roomService.calculateRoomTotalPrice(roomTypeId,checkIn,checkOut,roomCount);
        Map<String,String> map = roomService.calculateRoomTotalPrice(roomTypeId,checkIn,checkOut,roomCount);
        hotelOrderDTO.setRawData(map.get("data"));
        hotelOrderDTO.setSignature(map.get("signature"));

        String orderId = hotelOrderService.createHotelOrder(hotelOrderDTO,currentUserId);
        if (orderId == null){
            return "创建订单失败，请稍后重试。";
        }
        return "订单创建成功，订单ID为："+orderId+"。请及时完成支付，30分钟后订单若未支付将会被系统自动取消。";
    }


}
