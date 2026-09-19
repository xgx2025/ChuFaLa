package com.hope.chufala.controller;

import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.util.EmailVerificationCodeUtils;
import com.hope.chufala.common.util.JwtTokenUtils;
import com.hope.chufala.domain.dto.LoginFormDTO;
import com.hope.chufala.domain.dto.RegisterFormDTO;
import com.hope.chufala.domain.entity.User;
import com.hope.chufala.common.domain.vo.Result;
import com.hope.chufala.service.IUserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private IUserService userService;
    @Autowired
    private EmailVerificationCodeUtils emailVerificationCodeUtils;
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
        User user = userService.login(loginFormDTO.getEmail(), loginFormDTO.getPassword());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("status", user.getStatus());
        String accessToken = JwtTokenUtils.generateAccessToken(claims);
        String refreshToken = JwtTokenUtils.generateRefreshToken(claims);
        Map<String,String> tokens = Map.of("accessToken",accessToken,"refreshToken",refreshToken);
        return Result.ok(tokens);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterFormDTO registerFormDTO, HttpServletRequest request) {
        request.getSession().removeAttribute("captcha");
        userService.register(registerFormDTO,request.getRemoteAddr());
        return Result.ok(null);
    }

    @GetMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestParam String email, HttpServletRequest request) {
        emailVerificationCodeUtils.generateAndSendCode(email,request.getRemoteAddr());
        return Result.ok(null);
    }


    @PostMapping("/refreshToken")
    public Result refreshToken(@RequestBody Map<String, String> params, HttpServletRequest request, HttpServletResponse response) {
        //TODO 校验刷新token是否在redis（或者数据库）白名单中
        String refreshToken = params.get("refreshToken");
        try {
            Claims claims = JwtTokenUtils.getClaimsFromToken(refreshToken, JwtTokenUtils.REFRESH_TOKEN_SECRET);
            Map<String,Object> map = new HashMap<>();
            map.put("userId",claims.get("userId"));
            map.put("status",claims.get("status"));
            String newAccessToken = JwtTokenUtils.generateAccessToken(map);
            String newRefreshToken = JwtTokenUtils.generateRefreshToken(map);
            Map<String,Object> result = Map.of("accessToken",newAccessToken,"refreshToken",newRefreshToken);
            return Result.ok(result);
        }catch (Exception e){
            response.setStatus(401);
            log.warn("用户登录已过期，用户ip:{}",request.getRemoteAddr());
            return  Result.fail(ResultCode.FAIL,"登录已过期,请重新登录！");
        }
    }
}
