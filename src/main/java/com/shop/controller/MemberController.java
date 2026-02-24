package com.shop.controller;

import com.shop.dto.LoginRequest;
import com.shop.dto.MemberRequest;
import com.shop.dto.PasswordChangeRequest;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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

    @PostMapping("/user/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal String userName, // 토큰에서 추출된 유저 아이디
            @RequestBody PasswordChangeRequest request) {

        memberService.updatePassword(userName, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().body("비밀번호 변경 완료");
    }
}
