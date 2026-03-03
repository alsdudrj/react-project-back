package com.shop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    //토큰 서명에 사용할 비밀 키
    private final String secretString = "HELLO_MYNAME_IS_VERY_LONG_SECRET_KEY_FOR_SECURITY";

    //HMAC SHA 알고리즘을 위한 SecretKey 객체 생성
    private final SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

    //사용자 정보를 바탕으로 JWT 토큰 생성
    public String createToken(String userName, Long id, String role, String displayName, String email) {
        return Jwts.builder()
                .setSubject(userName)                  // 토큰 제목 (userName)
                .claim("id", id)                       // 사용자 PK 저장
                .claim("auth", role)                   // 권한 정보 저장
                .claim("nickname", displayName)        // 닉네임 저장
                .claim("email", email)                 // 이메일 저장
                .setIssuedAt(new Date())               // 토큰 발행 시간
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간
                .signWith(key)                         // 비밀 키로 서명
                .compact();                            // 토큰 생성 및 압축
    }

    //JWT 토큰을 해독하여 담겨 있는 정보를 추출
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)                    // 검증에 사용할 키 설정
                .build()
                .parseClaimsJws(token)                 // 토큰 해석 및 서명 확인
                .getBody();                            // 클레임 데이터 반환
    }
}
