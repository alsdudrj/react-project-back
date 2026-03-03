package com.shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Spring Security 상세 보안 설정 클래스
 * 인증(Authentication) 및 인가(Authorization) 정책을 정의
 */
@Configuration
@EnableWebSecurity  //Spring Security 지원을 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())    //설정한 CorsConfig를 따름
                .csrf(csrf -> csrf.disable())//REST API이므로 CSRF 보호 비활성화

                //로그인설정
                //JWT를 사용하므로 폼 로그인, 기본 HTTP 인증, 세션 생성을 모두 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //API 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/health", "/user/login", "/user/social-login",
                                "/user/social-login/kakao", "/user/register", "/item/all",
                                "/payment/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/item/**").permitAll()
                        .requestMatchers("/item/edit/**", "/item/add", "/admin/**", "/user/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/me", "/user/change-password").authenticated()
                        .anyRequest().authenticated()
                );

        //JWT 필터 위치 지정
        //사용자 이름/비밀번호 인증 필터(UsernamePasswordAuthenticationFilter) 전에 JWT 검증을 수행
        http.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 Encoder 빈 등록
     * BCrypt 해시 함수를 사용하여 비밀번호를 안전하게 저장
     */
    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
