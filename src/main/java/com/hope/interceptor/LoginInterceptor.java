package com.hope.interceptor;

import com.hope.utils.JwtTokenUtil;
import com.hope.utils.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String fullPath = request.getRequestURI();
        System.out.println("实际路径："+fullPath);
        String authorization = request.getHeader("Authorization");
        try{
            System.out.println("通过");
            String token =authorization.substring(7).trim();
            Claims claims = JwtTokenUtil.getClaimsFromToken(token, JwtTokenUtil.ACCESS_TOKEN_SECRET);
            ThreadLocalUtil.set(claims);
            return true;
        }catch (Exception e){
            System.out.println("失败");
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
