package com.shop.controller;

import com.shop.dto.ItemRequestDto;
import com.shop.entity.Item;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    //상품 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        Item item = itemService.findById(id);
        return ResponseEntity.ok(item);
    }

    //상품표시
    @GetMapping("/all")
    public ResponseEntity<List<Item>> getAllItems() {
        //DB에서 모든 상품 리스트를 가져와서 반환
        List<Item> items = itemService.findAll();
        return ResponseEntity.ok(items);
    }

    //상품추가
    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestBody ItemRequestDto itemRequestDto) {
        try {
            //프론트에서 받은정보 저장
            itemService.saveItem(itemRequestDto);
            return ResponseEntity.ok("상품 등록 완료");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("등록 실패: " + e.getMessage());
        }
    }

    //상품 수정
    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemRequestDto itemRequestDto) {
        try {
            itemService.updateItem(id, itemRequestDto);
            return ResponseEntity.ok("상품 수정 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("수정 실패: " + e.getMessage());
        }
    }

    //상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok().body("상품이 삭제되었습니다.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("삭제 실패: " + e.getMessage());
        }
    }
}

