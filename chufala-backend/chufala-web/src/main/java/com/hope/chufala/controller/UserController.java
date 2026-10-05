package com.hope.chufala.controller;


import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.model.dto.UserUpdateFormDTO;
import com.hope.chufala.model.entity.User;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.service.IUserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口。
 *
 * <p>提供当前用户信息查询、头像上传与资料更新；用户身份取自 ThreadLocal 中的 JWT claims。
 *
 * @author 谢光湘
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;
    /**
     * 查询当前登录用户信息。
     *
     * @return 用户信息
     */
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

    /**
     * 上传头像。
     *
     * @param avatar 头像文件
     * @return 头像访问 URL
     */
    @PutMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam("file") MultipartFile avatar) {
        String url = userService.uploadAvatar(avatar);
        return Result.ok(url);
    }

    /**
     * 更新当前用户资料（用户 ID 以登录态为准，忽略请求体中的 id）。
     *
     * @param user 待更新的资料
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result update(@RequestBody UserUpdateFormDTO user) {
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
