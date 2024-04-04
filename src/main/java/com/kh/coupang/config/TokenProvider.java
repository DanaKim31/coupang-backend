package com.kh.coupang.config;

import com.kh.coupang.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class TokenProvider {

    // secretKey 발급
    private SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // 토큰발급 메서드
    public String create(User user) {
        return Jwts.builder()
                .signWith(secretKey)
                // .setSubject(user.getId())  // setSubject : 하나의 값만 담음
                .setClaims(Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ))
                .setIssuedAt(new Date()) // 토큰의 기간 설정
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .compact();
    }

    //
    public User validateGetUser(String token) {
        Claims claims = Jwts.parser()
                            .setSigningKey(secretKey)
                            .parseClaimsJws(token)
                            .getBody();
        return User.builder()
                // create 메서드에서 setClaims(Map.of())로 담아둔 후 불러오기 가능
                .id((String) claims.get("id")) // get 메서드의 "id" = user.getId()
                .name((String) claims.get("name"))
                .email((String) claims.get("email"))
                .role((String) claims.get("role"))
                .build();
    }

}
