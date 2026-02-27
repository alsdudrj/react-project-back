package com.shop.service;

import com.shop.dto.MemberResponseDTO;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;

    //전체 회원 조회
    public List<MemberResponseDTO> findAllMember(){
        List<Member> members = memberRepository.findAll();

        return members.stream()
                .map(member -> new MemberResponseDTO(
                    member.getId(),
                    member.getUserName(),
                    member.getEmail(),
                    member.getAddress(),
                    member.getDetailAddress()
                ))
                .toList();
    }
    
    //관리자 회원삭제
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "탈퇴하려는 유저를 찾을 수 없습니다.");
        }

        memberRepository.deleteById(id);
    }
}
