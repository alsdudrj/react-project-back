package com.shop.service;

import com.shop.dto.LoginRequest;
import com.shop.dto.MemberRequest;
import com.shop.entity.Member;
import com.shop.entity.Role;
import com.shop.repository.MemberRepository;
import com.shop.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    //회원가입
    public void register(MemberRequest memberRequest) {
        if (memberRepository.existsByUserName(memberRequest.getUserName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 아이디");
        }

        Member member = new Member();
        member.setUserName(memberRequest.getUserName());
        member.setPassword(passwordEncoder.encode(memberRequest.getPassword()));
        member.setDisplayName(memberRequest.getDisplayName());

        //권한 설정
        Role role = Role.valueOf("ROLE_" + memberRequest.getAuth());
        member.setAuth(role);

        memberRepository.save(member);
    }

    //로그인
    public String login(LoginRequest dto) {
        Member member = memberRepository.findByUserName(dto.getUserName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 없거나 비밀번호 다름"));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 없거나 비밀번호 다름");
        }

        return jwtUtil.createToken(member.getUserName(), member.getAuth().name());
    }
}
