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
                member.getId(),
                member.getAuth().name(),
                member.getDisplayName(),
                member.getEmail()
        );
    }

    //소셜 로그인
    @Transactional
    public String processSocialLogin(SocialLoginRequest request) {
        //기존 회원인지 확인
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

        //서버의 JWT 토큰 발행
        return jwtUtil.createToken(
                member.getUserName(),
                member.getId(),
                member.getAuth().name(),
                member.getDisplayName(),
                member.getEmail()
        );
    }

    //비밀번호 변경
    @Transactional
    public void updatePasswordById(Long id, String currentPassword, String newPassword) {
        Member member = memberRepository.findById(id)
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

    //컨트롤러에서 사용하기 위한 엔티티 조회 메서드
    public Member getMemberByUsername(String username) {
        return memberRepository.findByUserName(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
    }

    //내 정보 조회
    public MemberResponseDTO getMyInfoById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));

        return new MemberResponseDTO(
                member.getUserName(),
                member.getEmail(),
                member.getAddress(),
                member.getDetailAddress()
        );
    }

    //회원정보 수정
    @Transactional
    public void updateMemberInfoById(Long id, MemberResponseDTO updateDTO) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        member.setEmail(updateDTO.getEmail());
        member.setAddress(updateDTO.getAddress());
        member.setDetailAddress(updateDTO.getDetailAddress());
    }

    //회원탈퇴
    @Transactional
    public void withdrawMemberById(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "탈퇴하려는 유저를 찾을 수 없습니다.");
        }
        memberRepository.deleteById(id);
    }
}
