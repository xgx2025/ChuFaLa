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
 *
 * <p>签发 access / refresh 两类 Token（HS256，各自独立密钥），并提供解析与过期判断；
 * claims 中携带 userId 与 status。
 *
 * @author 谢光湘
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

    /**
     * 获取 Access Token 签名密钥。
     *
     * @return 密钥
     */
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    /**
     * 获取 Refresh Token 签名密钥。
     *
     * @return 密钥
     */
    public String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    /**
     * 生成Access Token
     *
     * @param claims 载荷（含 userId、status）
     * @return Access Token
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
     *
     * @param claims 载荷（含 userId、status）
     * @return Refresh Token
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
     *
     * @param token Access Token
     * @return 载荷
     */
    public Claims getClaimsFromAccessToken(String token) {
        return getClaimsFromToken(token, accessTokenSecret);
    }

    /**
     * 解析Refresh Token
     *
     * @param token Refresh Token
     * @return 载荷
     */
    public Claims getClaimsFromRefreshToken(String token) {
        return getClaimsFromToken(token, refreshTokenSecret);
    }

    /**
     * 验证Token并获取claims
     *
     * @param token  Token
     * @param secret 对应密钥
     * @return 载荷
     */
    public Claims getClaimsFromToken(String token, String secret) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查Token是否过期
     *
     * <p>仅把 ExpiredJwtException 视为过期；其他异常（如签名不合法）向上抛出，
     * 避免把「被篡改的 token」误判为「已过期」而放行。
     *
     * @param token  Token
     * @param secret 对应密钥
     * @return 已过期返回 true
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
