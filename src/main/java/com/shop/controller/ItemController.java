package com.shop.controller;

import com.shop.entity.Item;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item") // 에러 메시지에 /item/add 라고 떴으니 /item으로 설정
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestBody Item item) {
        try {
            //프론트에서 받은정보 저장
            itemService.saveItem(item);
            return ResponseEntity.ok("상품 등록 완료");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("등록 실패: " + e.getMessage());
        }
    }
}
