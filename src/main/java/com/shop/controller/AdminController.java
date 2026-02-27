package com.shop.controller;

import com.shop.dto.MemberRequest;
import com.shop.dto.MemberResponseDTO;
import com.shop.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/admin")
public class AdminController {

    private final AdminService adminService;

    //전체 회원 조회
    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> getAllMember(){
        return ResponseEntity.ok(adminService.findAllMember());
    }

    //관리자 회원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable Long id){
        adminService.deleteMember(id);
        return ResponseEntity.ok("회원이 정상적으로 삭제 되었습니다.");
    }
}
