package com.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS(Cross-Origin Resource Sharing) 설정 클래스
 * 다른 도메인(React 프론트엔드)에서의 API 요청을 허용하기 위한 보안 설정
 */
@Configuration
public class CorsConfig {

    @Value("${react.api.api.url}")
    private String reactBaseUrl;    //프론트 엔드 주소

    /**
     * CORS 정책을 정의하는 Bean 설정
     * @return 구체적인 CORS 정책이 담긴 source 객체
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(   //내 서버에 접근을 허용할 특정 도메인 설정
                reactBaseUrl
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));   //허용할 HTTP Method 설정
        config.setAllowedHeaders(List.of("*")); //모든 HTTP Header 허용
        config.setExposedHeaders(List.of("Authorization")); //클라이언트(브라우저)에서 접근 가능한 헤더 설정
        config.setAllowCredentials(true);   //쿠키나 인증 정보를 포함한 요청을 허용할지 여부

        //모든 경로("/**")에 대해 위의 설정을 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
