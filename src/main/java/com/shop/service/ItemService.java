package com.shop.service;

import com.shop.dto.ItemRequestDto;
import com.shop.dto.SizeStockDto;
import com.shop.entity.Item;
import com.shop.entity.ItemSizeStock;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

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

    //상품 전체조회
    public List<Item> findAll(){
        return itemRepository.findAll();
    }

    //상품삭제
    public void deleteItem(long id){
        if (!itemRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 상품 아이디입니다: " + id);
        }

        itemRepository.deleteById(id);
    }

    //상품 단건조회
    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 없음"));
    }
}
