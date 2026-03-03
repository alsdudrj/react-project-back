package com.shop.service;

import com.shop.entity.Member;
import com.shop.entity.Role;
import com.shop.repository.MemberRepository;
import com.shop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;


/**
 * 카카오 소셜 로그인 비즈니스 로직 서비스
 * OAuth 2.0 프로토콜을 사용하여 카카오 사용자 인증 및 자동 회원가입/로그인을 처리
 */
@Service
@RequiredArgsConstructor
public class KakaoMemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${kakao.rest.api.key}") //application.properties에 설정
    private String kakaoRestApiKey;
    @Value("${kakao.client.secret}") //application.properties에 설정
    private String kakaoClientSecret;

    @Transactional
    public String processKakaoLogin(String code, String redirectUri) {
        //인가 코드를 이용해 카카오로부터 Access Token 받기
        String accessToken = getKakaoAccessToken(code, redirectUri);
        //Access Token으로 유저정보 추출
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        //사용자 정보 파싱 (고유 식별자, 이메일, 닉네임 등)
        String kakaoId = userInfo.get("id").toString();
        String providerId = "KAKAO_" + kakaoId; //중복 방지를 위한 식별자 생성

        //데이터 파싱
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        //이메일 추출
        String email = (kakaoAccount.get("email") != null)
                ? kakaoAccount.get("email").toString()
                : kakaoId + "@kakao.com"; //현재 카카오 이메일열람 권한이 없어서 아이디를 활용한 이메일 생성
        //닉네임 추출
        String nickname = (profile != null && profile.get("nickname") != null)
                ? profile.get("nickname").toString()
                : "카카오유저_" + kakaoId;

        //DB 확인 및 회원가입 (식별자로 회원 검색)
        Member member = memberRepository.findByUserName(providerId)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .userName(providerId) //고정된 식별자 저장
                            .email(email)        //초기 이메일 저장
                            .displayName(nickname)
                            .auth(Role.ROLE_USER)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString())) //신규 가입 시 비밀번호는 무작위 UUID로 생성
                            .build();
                    return memberRepository.save(newMember);
                });

        //서버 전용 JWT 생성
        return jwtUtil.createToken(
                member.getUserName(),
                member.getId(),
                member.getAuth().name(),
                member.getDisplayName(),
                member.getEmail()
        );
    }

    /**
     * 카카오 토큰 서버에 액세스 토큰 요청
     * [POST] https://kauth.kakao.com/oauth/token
     */
    private String getKakaoAccessToken(String code, String redirectUri) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoRestApiKey);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", kakaoClientSecret);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    /**
     * 카카오 API 서버에 사용자 프로필 정보 요청
     * [POST] https://kapi.kakao.com/v2/user/me
     */
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);
        ResponseEntity<Map> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                Map.class
        );

        return response.getBody();
    }
}
