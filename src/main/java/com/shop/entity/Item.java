package com.shop.entity;

import com.shop.dto.ItemRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer price;
    private String content;

    // 추가할 필드들
    private String category;
    private String producer;
    private String origin;
    private String shipping;
    private String imgUrl;

    //사이즈에 대한 1:N FK 설정
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemSizeStock> sizeStocks = new ArrayList<>();

    public void addSizeStock(ItemSizeStock itemSizeStock) {
        this.sizeStocks.add(itemSizeStock);
        itemSizeStock.setItem(this);
    }

    //상품 수정을 위한 비즈니스 로직
    public void updateItem(ItemRequestDto dto) {
        this.title = dto.getTitle();
        this.price = dto.getPrice();
        this.content = dto.getContent();
        this.imgUrl = dto.getImgUrl();
        this.origin = dto.getOrigin();
        this.producer = dto.getProducer();
        this.shipping = dto.getShipping();
        this.category = dto.getCategory();

        // 사이즈/재고 리스트 업데이트 로직
        this.sizeStocks.clear();
        if (dto.getSizeStocks() != null) {
            dto.getSizeStocks().forEach(s -> {
                ItemSizeStock stock = new ItemSizeStock();
                stock.setSize(s.getSize());
                stock.setStock(s.getStock());
                
                //양방향 연관관계로 자식에게 this(부모)를 알려줌
                stock.setItem(this);
                
                this.sizeStocks.add(stock);
            });
        }
    }
}
