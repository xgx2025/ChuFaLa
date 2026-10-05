package com.hope.chufala.controller;

import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.service.IMembershipService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员（VIP）接口。
 *
 * @author 谢光湘
 */
@RestController
@RequestMapping("/membership")
public class MembershipController {

    @Autowired
    private IMembershipService membershipService;

    /**
     * 创建会员购买订单。
     *
     * @return 支付表单或支付链接
     */
    @PostMapping
    public Result createMembershipOrder() {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        String orderId =  membershipService.createMembershipOrder(userId);
        return Result.ok(orderId);
    }

}
