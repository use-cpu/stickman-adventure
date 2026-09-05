package com.stickman.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成 token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.CTX_USER_ID, userId);
        claims.put(Constants.CTX_USERNAME, username);
        claims.put(Constants.CTX_ROLE, role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 解析 token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : Long.valueOf(claims.get(Constants.CTX_USER_ID).toString());
    }

    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.getSubject();
    }

    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get(Constants.CTX_ROLE).toString();
    }

    public boolean isExpired(String token) {
        return parseToken(token) == null;
    }
}
