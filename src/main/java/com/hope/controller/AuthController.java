package com.hope.controller;

import com.hope.constant.ResultCode;
import com.hope.domain.dto.LoginFormDTO;
import com.hope.domain.entity.User;
import com.hope.domain.vo.Result;
import com.hope.service.IUserService;
import com.hope.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private IUserService userService;
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
        User user = userService.login(loginFormDTO.getEmail(), loginFormDTO.getPassword());
        if (user == null ){
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        String accessToken = JwtTokenUtil.generateAccessToken(claims);
        String refreshToken = JwtTokenUtil.generateRefreshToken(claims);
        Map<String,String> tokens = Map.of("access_token",accessToken,"refresh_token",refreshToken);
        return Result.ok(tokens);
    }
}
