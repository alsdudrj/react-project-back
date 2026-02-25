package com.shop.controller;

import com.shop.dto.LoginRequest;
import com.shop.dto.MemberRequest;
import com.shop.dto.MemberResponseDTO;
import com.shop.dto.PasswordChangeRequest;
import com.shop.entity.Member;
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

    //비밀번호 변경
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal String userName, // 토큰에서 추출된 유저 아이디
            @RequestBody PasswordChangeRequest request) {

        memberService.updatePassword(userName, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().body("비밀번호 변경 완료");
    }

    //내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //인증 정보가 없거나 로그인하지 않은 상태 체크
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String username = authentication.getName(); // 토큰에 저장된 유저 ID/이름이 나옵니다.

        MemberResponseDTO myInfo = memberService.getMyInfo(username);
        return ResponseEntity.ok(myInfo);
    }
    
    //회원정보 수정
    @PutMapping("/me")
    public ResponseEntity<?> updateMyInfo(@RequestBody MemberResponseDTO updateDTO) {
        //SecurityContext에서 현재 로그인한 유효한 사용자의 ID를 추출
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            //정보 업데이트
            memberService.updateMemberInfo(username, updateDTO);
            return ResponseEntity.ok("정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("수정 중 오류 발생: " + e.getMessage());
        }
    }

    //회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<?> withdraw() {
        //SecurityContext에서 현재 로그인한 사용자의 ID 추출
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            //삭제 로직
            memberService.withdrawMember(username);

            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("탈퇴 처리 중 오류 발생: " + e.getMessage());
        }
    }
}
