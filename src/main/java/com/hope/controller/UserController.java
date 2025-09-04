package com.hope.controller;

import com.hope.constant.ResultCode;
import com.hope.domain.entity.User;
import com.hope.domain.vo.Result;
import com.hope.service.IUserService;
import com.hope.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;
    @GetMapping("/info")
    public Result info() {
        Claims claims =  ThreadLocalUtil.get();
        Long userId = claims.get("userId", Long.class);
        User user = userService.getUserById(userId);
        if(user == null){
            return Result.fail(ResultCode.INTERNAL_SERVER_ERROR);
        }
        return Result.ok(user);
    }
}
