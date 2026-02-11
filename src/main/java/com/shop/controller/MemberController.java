package com.shop.controller;

import com.shop.dto.LoginRequest;
import com.shop.dto.MemberRequest;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
