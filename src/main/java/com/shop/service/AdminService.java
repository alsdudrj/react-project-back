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
    @Transactional(readOnly = true) //단순 조회 작업이므로 성능 최적화를 위해 readOnly 설정
    public List<MemberResponseDTO> findAllMember(){
        //DB에서 모든 회원 엔티티 조회
        List<Member> members = memberRepository.findAll();

        //Entity 리스트를 Stream을 사용하여 DTO 리스트로 매핑
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
        //삭제 전 해당 유저의 존재 여부 확인
        if (!memberRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "탈퇴하려는 유저를 찾을 수 없습니다.");
        }

        memberRepository.deleteById(id);
    }
}
