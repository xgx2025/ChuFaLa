package com.hope.chufala.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;


public class JwtTokenUtils {

    // Access Token密钥和过期时间(30分钟)
    // 签名密钥通过环境变量注入，不再硬编码在源码中
    public static final String ACCESS_TOKEN_SECRET = requireEnv("JWT_ACCESS_TOKEN_SECRET");
    public static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000;




    // Refresh Token密钥和过期时间(7天)
    public static final String REFRESH_TOKEN_SECRET = requireEnv("JWT_REFRESH_TOKEN_SECRET");
    public static final long REFRESH_TOKEN_EXPIRE = 2 * 24 * 60 * 60 * 1000;

    /**
     * 读取必需的环境变量，缺失时立即失败，避免回退到弱默认密钥
     */
    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "缺少必需的环境变量 [" + name + "]，请参考 README 配置后重试");
        }
        return value;
    }


    /**
     * 生成Access Token
     */
    public static String generateAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, ACCESS_TOKEN_SECRET)
                .compact();
    }

    /**
     * 生成Refresh Token
     */
    public static String generateRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, REFRESH_TOKEN_SECRET)
                .compact();
    }

    /**
     * 验证Token并获取claims
     */
    public static Claims getClaimsFromToken(String token, String secret) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查Token是否过期
     */
    public static boolean isTokenExpired(String token, String secret) {
        try {
            Claims claims = getClaimsFromToken(token, secret);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // 明确捕获过期异常
            return true;
        } catch (Exception e) {
            // 其他异常不直接视为过期，而是抛出具体异常
            throw new IllegalArgumentException("token验证失败: " + e.getMessage());
        }
    }


}