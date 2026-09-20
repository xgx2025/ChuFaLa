package com.hope.chufala.controller;

import com.hope.chufala.common.domain.vo.Result;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.service.IAlipayService;
import com.hope.chufala.service.IMembershipService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/membership")
public class MembershipController {

    @Autowired
    private IMembershipService membershipService;

    @PostMapping
    public Result createMembershipOrder() {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        String orderId =  membershipService.createMembershipOrder(userId);
        return Result.ok(orderId);
    }

}
