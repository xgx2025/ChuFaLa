package com.hope.chufala.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * JWT 工具
 * 签名密钥统一由 application.yml 的 jwt.* 提供，不在源码中硬编码
 */
@Component
public class JwtTokenUtils {

    /** Access Token 签名密钥 */
    @Value("${jwt.access-token-secret}")
    private String accessTokenSecret;

    /** Refresh Token 签名密钥 */
    @Value("${jwt.refresh-token-secret}")
    private String refreshTokenSecret;

    // Access Token过期时间(2小时)
    private static final long ACCESS_TOKEN_EXPIRE = 2 * 60 * 60 * 1000;

    // Refresh Token过期时间(2天)
    private static final long REFRESH_TOKEN_EXPIRE = 2 * 24 * 60 * 60 * 1000;

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    /**
     * 生成Access Token
     */
    public String generateAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, accessTokenSecret)
                .compact();
    }

    /**
     * 生成Refresh Token
     */
    public String generateRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(SignatureAlgorithm.HS256, refreshTokenSecret)
                .compact();
    }

    /**
     * 解析Access Token
     */
    public Claims getClaimsFromAccessToken(String token) {
        return getClaimsFromToken(token, accessTokenSecret);
    }

    /**
     * 解析Refresh Token
     */
    public Claims getClaimsFromRefreshToken(String token) {
        return getClaimsFromToken(token, refreshTokenSecret);
    }

    /**
     * 验证Token并获取claims
     */
    public Claims getClaimsFromToken(String token, String secret) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token, String secret) {
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
