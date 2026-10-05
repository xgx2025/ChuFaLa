package com.hope.chufala.agent.tool;

import com.hope.chufala.model.dto.HotelOrderDTO;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.model.entity.User;
import com.hope.chufala.service.IAiConversationService;
import com.hope.chufala.service.IHotelOrderService;
import com.hope.chufala.service.IRoomService;
import com.hope.chufala.service.IUserService;
import com.hope.chufala.security.ToolUserContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 酒店订单工具，供智能助手在对话中查询或创建订单。
 *
 * <p>用户身份一律通过 ToolContext 传递（见 {@link ToolUserContext}），
 * 不接受模型传入的 userId，避免模型伪造身份越权访问他人订单。
 *
 * @author 谢光湘
 */
@SuppressWarnings("all")
@Component
public class OrderTools {
    @Autowired
    private IHotelOrderService hotelOrderService;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private IUserService userService;



    /**
     * 按状态/下单时间查询当前用户的酒店订单。
     *
     * @param orderStatus 订单状态，可为空
     * @param bookTime    下单时间，可为空
     * @param toolContext 工具上下文（含当前用户 ID）
     * @return 订单列表文本
     */
    @Tool(description = "查询当前用户的酒店订单。可按状态筛选：'待支付'、'已支付'、'已取消'，如果用户未提状态，就查所有订单。可按时间筛选，如果用户没有指明时间，就将时间设置null，不用询问用户时间")
    public String queryHotelOrderByUserIdPage(@ToolParam(description = "订单状态,状态有‘待支付’、‘已支付’、‘已取消’") String orderStatus, @ToolParam(description = "下单时间") LocalDate bookTime, ToolContext toolContext) {
        Long currentUserId = ToolUserContext.getUserId(toolContext);
        List<HotelOrder> orderPageResult = hotelOrderService.findHotelOrdersByUserIdWithConditions(currentUserId,orderStatus,bookTime);
       return orderPageResult.toString();
    }

    /**
     * 按订单 ID 查询订单详情（自动限定为当前用户）。
     *
     * @param orderId     业务订单 ID
     * @param toolContext 工具上下文（含当前用户 ID）
     * @return 订单详情文本，不属于当前用户时返回提示语
     */
    @Tool(description = "通过订单ID查询酒店订单详情")
    public String queryHotelOrderByOrderId(@ToolParam(description = "订单ID") Long orderId, ToolContext toolContext) {
        Long currentUserId = ToolUserContext.getUserId(toolContext);
        HotelOrder hotelOrder = hotelOrderService.getByOrderIdAndUserId(orderId,currentUserId);
        if (hotelOrder == null){
            return "未找到该订单";
        }
        return hotelOrder.toString();
    }

    /**
     * 创建酒店订单。
     *
     * <p>入住人信息取自当前登录用户；价格由服务端计算并签名后随 rawData/signature 提交。
     *
     * @param checkIn     入住日期
     * @param checkOut    离店日期
     * @param roomCount   房间数
     * @param roomTypeId  房型 ID
     * @param arrivalTime 预计到店时间
     * @param toolContext 工具上下文（含当前用户 ID）
     * @return 结果说明文本（含订单号与支付时限）
     */
    @Tool(description = "创建酒店订单")
    public String createHotelOrder(@ToolParam(description = "入住时间") LocalDate checkIn, @ToolParam(description = "退房时间（当晚可住）") LocalDate checkOut, @ToolParam(description = "预定房间数量") Integer roomCount, @ToolParam(description = "房型ID") Long roomTypeId, @ToolParam(description = "预计到店时间，格式如14:00") String arrivalTime, ToolContext toolContext) {
        HotelOrderDTO hotelOrderDTO = new HotelOrderDTO(null,null,null,null,null,checkIn.toString(),null);
        Long currentUserId = ToolUserContext.getUserId(toolContext);
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
