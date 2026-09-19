package com.hope.chufala.controller;


import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.domain.dto.UserUpdateFromDTO;
import com.hope.chufala.domain.entity.User;
import com.hope.chufala.common.domain.vo.Result;
import com.hope.chufala.service.IUserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;
    @GetMapping("/info")
    public Result info() {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        User user = userService.getUserById(userId);
        if(user == null){
            return Result.fail(ResultCode.INTERNAL_SERVER_ERROR);
        }
        return Result.ok(user);
    }

    @PutMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam("file") MultipartFile avatar) {
        String url = userService.uploadAvatar(avatar);
        return Result.ok(url);
    }

    @PutMapping("/update")
    public Result update(@RequestBody UserUpdateFromDTO user) {
        Claims claims =  ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        user.setId(userId);
        System.out.println(user);
        boolean flag = userService.updateById(user);
        if(!flag){
            return Result.fail(ResultCode.INTERNAL_SERVER_ERROR);
        }
        return Result.ok(null);
    }


}
