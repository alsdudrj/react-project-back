package com.shop.service;

import com.shop.dto.ItemRequestDto;
import com.shop.dto.SizeStockDto;
import com.shop.entity.Item;
import com.shop.entity.ItemSizeStock;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    //상품 추가
    public void saveItem(ItemRequestDto dto){
        Item item = new Item();

        item.setTitle(dto.getTitle());
        item.setPrice(dto.getPrice());
        item.setContent(dto.getContent());
        item.setCategory(dto.getCategory());
        item.setProducer(dto.getProducer());
        item.setOrigin(dto.getOrigin());
        item.setShipping(dto.getShipping());
        item.setImgUrl(dto.getImgUrl());

        //사이즈 정보 변환 및 연결
        for (SizeStockDto sizeDto : dto.getSizeStocks()) {
            ItemSizeStock sizeStock = new ItemSizeStock();
            sizeStock.setSize(sizeDto.getSize());
            sizeStock.setStock(sizeDto.getStock());

            //Item 엔티티에 사이즈 추가
            item.addSizeStock(sizeStock);
        }

        //cascade 설정으로 사이즈 정보도 같이 저장 가능
        itemRepository.save(item);
    }

    //상품 단건조회
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 없음"));
    }

    //상품 전체조회
    public List<Item> findAll(){
        return itemRepository.findAll();
    }

    //상품 수정
    public void updateItem(Long id, ItemRequestDto dto) {
        //기존 상품 조회
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. id=" + id));

        //엔티티 데이터 업데이트
        item.updateItem(dto);

        // Transactional 어노테이션 덕분에 메서드 종료 시 자동으로 DB에 반영됩니다.
    }

    //상품삭제
    public void deleteItem(long id){
        if (!itemRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 상품 아이디입니다: " + id);
        }

        itemRepository.deleteById(id);
    }


}
