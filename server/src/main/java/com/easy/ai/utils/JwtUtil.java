package com.easy.ai.utils;

import com.easy.ai.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private static String key = "easy-deepseek-very-strong-secret-key-longer-than-32-bytes";

    public static SecretKey createKey() {
        return Keys.hmacShaKeyFor(key.getBytes());
    }

    public static String getToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getUserId())
                .signWith(createKey())
                .expiration(new Date(System.currentTimeMillis() + 7200*1000))
                .compact();
    }

    public static Claims parseToken(String token){
        if(token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        return Jwts.parser()
                .setSigningKey(createKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
