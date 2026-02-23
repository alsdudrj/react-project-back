package com.shop.config;

import com.shop.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 Authorization 추출
        String authorization = request.getHeader("Authorization");

        // 토큰이 없거나 Bearer로 시작하지 않으면 통과
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰만 추출
        String token = authorization.split(" ")[1];

        try {
            // JwtUtil을 사용해 토큰 해석
            Claims claims = jwtUtil.parseToken(token);
            String userName = claims.getSubject();
            String role = (String) claims.get("auth");

            // Spring Security 전용 인증 객체 만들기
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userName, null, List.of(new SimpleGrantedAuthority(role)));

            // 인증
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (Exception e) {
            // 토큰이 가짜거나 만료된 경우
        }

        filterChain.doFilter(request, response);
    }
}