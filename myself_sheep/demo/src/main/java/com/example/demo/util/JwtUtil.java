package com.example.demo.util;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // 单位：毫秒

    /**
     * 从token中提取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从token中提取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定的声明
     *
     * @param token JWT令牌
     * @param claimsResolver 声明解析器
     * @param <T> 声明类型
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中提取所有声明
     *
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims extractAllClaims(String token) {
        // 关键修改：使用 Jwts.parserBuilder()，这是 JJWT 0.11.0+ 的标准用法
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     *
     * @param token JWT令牌
     * @return 如果过期返回true，否则返回false
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 为指定用户生成token
     *
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        // 关键修改：使用 Jwts.builder()，并明确指定签名算法
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 【新增】验证token是否有效（Spring Security标准方法） 这个方法会被JwtAuthenticationFilter调用
     *
     * @param token JWT令牌
     * @param userDetails Spring Security的用户详情对象
     * @return 如果有效返回true，否则返回false
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            // 检查用户名是否匹配，并且token是否过期
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            System.out.println("Token已过期: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("不支持的Token类型: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Token格式错误: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("Token签名验证失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Token参数错误: " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取签名密钥
     *
     * @return Key
     */
    private Key getSigningKey() {
        byte[] keyBytes = this.secret.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
