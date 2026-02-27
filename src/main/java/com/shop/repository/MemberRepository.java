package com.shop.repository;

import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUserName(String userName); //존재하는 아이디 인지 검사

    boolean existsById(Long id);

    Optional<Member> findByUserName(String userName);
}
