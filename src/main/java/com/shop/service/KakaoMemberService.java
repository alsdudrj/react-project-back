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
        //카카오로부터 Access Token 받기
        String accessToken = getKakaoAccessToken(code, redirectUri);
        //Access Token으로 유저정보 추출
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        //카카오 고유 ID
        String kakaoId = userInfo.get("id").toString();

        //데이터 파싱
        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        //이메일 추출
        String email = (kakaoAccount.get("email") != null)
                ? kakaoAccount.get("email").toString()
                : kakaoId + "@kakao.com"; //현재 이메일열람 권한이 없어서 아이디를 활용한 이메일 생성
        //닉네임 추출
        String nickname = (profile != null && profile.get("nickname") != null)
                ? profile.get("nickname").toString()
                : "카카오유저_" + kakaoId;

        //DB 확인 및 회원가입
        Member member = memberRepository.findByUserName(email)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .userName(email)
                            .email(email)
                            .displayName(nickname)
                            .auth(Role.ROLE_USER)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .build();
                    return memberRepository.save(newMember);
                });

        //서버 전용 JWT 생성
        return jwtUtil.createToken(
                member.getUserName(),
                member.getAuth().name(),
                member.getDisplayName(),
                member.getEmail()
        );
    }

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
