package com.shop.controller;

import com.shop.dto.*;
import com.shop.entity.Member;
import com.shop.service.KakaoMemberService;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final KakaoMemberService kakaoMemberService;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberRequest memberRequest){
        memberService.register(memberRequest);
        return ResponseEntity.ok("회원가입 완료");
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        String token = memberService.login(loginRequest);

        return ResponseEntity.ok(token);
    }
    
    //소셜로그인
    @PostMapping("/social-login")
    public ResponseEntity<?> socialLogin(@RequestBody SocialLoginRequest request) {
        //소셜 계정으로 로그인 또는 회원가입 처리 후 토큰 생성
        String token = memberService.processSocialLogin(request);

        //서버전용 JWT 토큰 반환
        return ResponseEntity.ok(token);
    }

    //소셜로그인(카카오)
    @PostMapping("/social-login/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        //코드를 이용해 카카오에서 정보를 가져오고 JWT 발급
        String token = kakaoMemberService.processKakaoLogin(request.getCode(), request.getRedirectUri());
        return ResponseEntity.ok(token);
    }

    //비밀번호 변경
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody PasswordChangeRequest request) {

        Long memberId = (Long) authentication.getDetails();

        memberService.updatePasswordById(memberId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().body("비밀번호 변경 완료");
    }

    //내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        //jwt id 조회
        Long memberId = (Long) authentication.getDetails();

        MemberResponseDTO myInfo = memberService.getMyInfoById(memberId);
        return ResponseEntity.ok(myInfo);
    }
    
    //회원정보 수정
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(Authentication authentication, @RequestBody MemberResponseDTO updateDTO) {
        //jwt id 조회
        Long memberId = (Long) authentication.getDetails();

        try {
            memberService.updateMemberInfoById(memberId, updateDTO);
            return ResponseEntity.ok("정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("수정 중 오류 발생: " + e.getMessage());
        }
    }

    //회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<?> withdraw(Authentication authentication) {
        //jwt id 조회
        Long memberId = (Long) authentication.getDetails();

        try {
            memberService.withdrawMemberById(memberId);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("탈퇴 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
