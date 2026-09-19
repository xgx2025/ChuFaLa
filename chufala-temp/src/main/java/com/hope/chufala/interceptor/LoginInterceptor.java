package com.hope.chufala.interceptor;


import com.hope.chufala.common.util.JwtTokenUtils;
import com.hope.chufala.common.util.ThreadLocalUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String fullPath = request.getRequestURI();
        log.info("实际路径：{}", fullPath);
        String authorization = request.getHeader("Authorization");
        try{
            log.info("通过");
            String token =authorization.substring(7).trim();
            Claims claims = JwtTokenUtils.getClaimsFromToken(token, JwtTokenUtils.ACCESS_TOKEN_SECRET);
            ThreadLocalUtils.set(claims);
            return true;
        }catch (Exception e){
            log.error("未登录", e);
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) throws Exception {
        ThreadLocalUtils.remove();
    }
}
