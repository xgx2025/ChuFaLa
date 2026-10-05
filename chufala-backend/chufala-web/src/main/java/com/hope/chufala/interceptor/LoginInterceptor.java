package com.hope.chufala.interceptor;


import com.hope.chufala.common.util.JwtTokenUtils;
import com.hope.chufala.common.util.ThreadLocalUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器。
 *
 * <p>从 Authorization 头解析 Bearer Token，校验通过后把 claims 放入
 * ThreadLocalUtils 供业务代码取当前用户；解析失败返回 401。
 * afterCompletion 中必须清除 ThreadLocal，否则线程复用会造成身份串号。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    /**
     * 请求前置校验：解析并缓存登录态。
     *
     * @param request  请求
     * @param response 响应（校验失败时置 401）
     * @param handler  处理器
     * @return 校验通过返回 true，否则 false
     * @throws Exception 处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String fullPath = request.getRequestURI();
        log.info("实际路径：{}", fullPath);
        String authorization = request.getHeader("Authorization");
        try{
            log.info("通过");
            String token =authorization.substring(7).trim();
            Claims claims = jwtTokenUtils.getClaimsFromAccessToken(token);
            ThreadLocalUtils.set(claims);
            return true;
        }catch (Exception e){
            log.error("未登录", e);
            response.setStatus(401);
            return false;
        }
    }

    /**
     * 请求完成后清除 ThreadLocal 中的登录态。
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       处理过程中的异常
     * @throws Exception 处理异常
     */
    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, Exception ex) throws Exception {
        ThreadLocalUtils.remove();
    }
}
