package com.shop.service;

import com.shop.dto.LoginRequest;
import com.shop.dto.MemberRequest;
import com.shop.dto.MemberResponseDTO;
import com.shop.dto.SocialLoginRequest;
import com.shop.entity.Member;
import com.shop.entity.Role;
import com.shop.repository.MemberRepository;
import com.shop.util.JwtUtil;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

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
        member.setAddress(memberRequest.getAddress());
        member.setDetailAddress(memberRequest.getDetailAddress());
        member.setDisplayName(memberRequest.getDisplayName());
        member.setEmail(memberRequest.getEmail());

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

        return jwtUtil.createToken(
                member.getUserName(),
                member.getAuth().name(),
                member.getDisplayName(),
                member.getEmail()
                );
    }

    //소셜 로그인
    @Transactional
    public String processSocialLogin(SocialLoginRequest request) {
        // 1. 기존 회원인지 확인 (userName 또는 email로 조회)
        Optional<Member> memberOpt = memberRepository.findByUserName(request.getUserName());

        Member member;
        if (memberOpt.isPresent()) {
            //이미 가입된 회원이라면 정보 가져오기
            member = memberOpt.get();
            
            //기존 회원의 displayName이 없다면 업데이트
            if (member.getDisplayName() == null) {
                member.setDisplayName(request.getDisplayName());
            }
        } else {
            //8자리 랜덤 난수 생성
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            
            //가입되지 않은 회원이라면 신규 회원가입 처리
            member = Member.builder()
                    .userName(request.getUserName())
                    .email(request.getEmail())
                    .displayName(request.getDisplayName())
                    .auth(Role.ROLE_USER) //기본 권한 부여
                    .password(passwordEncoder.encode(tempPassword)) //난수로 생성된 임시 비번
                    .build();
            memberRepository.save(member);
        }

        // 2. 우리 서버의 JWT 토큰 발행 (기존 로그인 로직에서 사용하던 토큰 생성 메서드 호출)
        return jwtUtil.createToken(
                member.getUserName(),
                member.getAuth().name(),
                member.getDisplayName(),
                member.getEmail()
        );
    }

    //비밀번호 변경
    @Transactional
    public void updatePassword(String userName, String currentPassword, String newPassword) {
        Member member = memberRepository.findByUserName(userName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저 없음"));

        //현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        //현재 비밀번호와 새 비밀번호가 같은지 체크
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "기존 비밀번호와 다른 비밀번호를 입력해주세요.");
        }

        //새 비밀번호 암호화 후 저장
        member.setPassword(passwordEncoder.encode(newPassword));
        //Transactional이 있으면 save를 따로 안 해도 DB에 반영 됨
    }

    //내 정보 조회
    public MemberResponseDTO getMyInfo(String username) {
        Member member = memberRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        //엔티티를 DTO로 변환해서 반환
        return new MemberResponseDTO(
                member.getUserName(),
                member.getEmail(),
                member.getAddress(),
                member.getDetailAddress()
        );
    }

    //회원정보 수정
    @Transactional
    public void updateMemberInfo(String username, MemberResponseDTO updateDTO) {
        //유저 조회
        Member member = memberRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        //필요한 정보 업데이트
        member.setEmail(updateDTO.getEmail());
        member.setAddress(updateDTO.getAddress());
        member.setDetailAddress(updateDTO.getDetailAddress());
    }

    //회원탈퇴
    @Transactional
    public void withdrawMember(String username) {
        //유저 존재유무 확인
        Member member = memberRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("탈퇴하려는 유저를 찾을 수 없습니다."));

        //삭제
        memberRepository.delete(member);
    }
}
