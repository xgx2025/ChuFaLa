package com.hope.controller;

import com.hope.constant.ResultCode;
import com.hope.domain.dto.LoginFormDTO;
import com.hope.domain.dto.RegisterFormDTO;
import com.hope.domain.entity.User;
import com.hope.domain.vo.Result;
import com.hope.service.IUserService;
import com.hope.utils.EmailVerificationCodeUtil;
import com.hope.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private IUserService userService;
    @Autowired
    private EmailVerificationCodeUtil emailVerificationCodeUtil;
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
        User user = userService.login(loginFormDTO.getEmail(), loginFormDTO.getPassword());
        if (user == null ){
            return Result.fail(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("status", user.getStatus());
        String accessToken = JwtTokenUtil.generateAccessToken(claims);
        String refreshToken = JwtTokenUtil.generateRefreshToken(claims);
        Map<String,String> tokens = Map.of("accessToken",accessToken,"refreshToken",refreshToken);
        return Result.ok(tokens);
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterFormDTO registerFormDTO) {
       return userService.register(registerFormDTO);
    }

    @GetMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestParam String email) {
        try {
            EmailVerificationCodeUtil.Result result = emailVerificationCodeUtil.generateAndSendCode(email);
            if (result.isSuccess()){
                return Result.ok(null);
            }else {
                return Result.fail(ResultCode.FAIL,result.getMessage());
            }
        }catch (MessagingException | UnsupportedEncodingException e){
            return Result.fail(ResultCode.FAIL,e.getMessage());
        }
    }


    @PostMapping("/refreshToken")
    public Result refreshToken(@RequestBody Map<String, String> params, HttpServletRequest request, HttpServletResponse response){
        String refreshToken = params.get("refreshToken");
        try {
            Claims claims = JwtTokenUtil.getClaimsFromToken(refreshToken,JwtTokenUtil.REFRESH_TOKEN_SECRET);
            Map<String,Object> map = new HashMap<>();
            map.put("userId",claims.get("userId"));
            map.put("status",claims.get("status"));
            String newAccessToken = JwtTokenUtil.generateAccessToken(map);
            String newRefreshToken = JwtTokenUtil.generateRefreshToken(map);
            Map<String,Object> result = new HashMap<>();
            result.put("accessToken",newAccessToken);
            result.put("refreshToken",newRefreshToken);
            return Result.ok(result);
        }catch (Exception e){
            response.setStatus(401);
            return  Result.fail(ResultCode.FAIL,"登录已过期,请重新登录！");
        }
    }
}
