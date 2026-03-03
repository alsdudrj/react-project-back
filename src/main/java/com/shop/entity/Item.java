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

    //추가할 필드들
    private String category;
    private String producer;
    private String origin;
    private String shipping;
    private String imgUrl;

    /**
     * 사이즈별 재고와의 1:N 연관관계 설정
     * mappedBy: 연관관계의 주인이 자식 엔티티(item)임을 명시
     * cascade = ALL: 부모(Item)가 저장/수정/삭제될 때 자식들도 함께 처리됨
     * orphanRemoval = true: 리스트에서 자식 객체를 제거하면 실제 DB에서도 삭제됨
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemSizeStock> sizeStocks = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     * 부모 객체에 자식을 추가할 때, 자식 객체에게도 부모가 누구인지 자동으로 설정
     * @param itemSizeStock 추가할 사이즈별 재고 객체
     */
    public void addSizeStock(ItemSizeStock itemSizeStock) {
        this.sizeStocks.add(itemSizeStock);
        itemSizeStock.setItem(this);
    }

    //상품 수정을 위한 비즈니스 로직
    //엔티티 내부에서 스스로의 상태를 변경하도록 응집도를 높임
    public void updateItem(ItemRequestDto dto) {
        this.title = dto.getTitle();
        this.price = dto.getPrice();
        this.content = dto.getContent();
        this.imgUrl = dto.getImgUrl();
        this.origin = dto.getOrigin();
        this.producer = dto.getProducer();
        this.shipping = dto.getShipping();
        this.category = dto.getCategory();

        //사이즈/재고 리스트 업데이트 로직
        //기존 리스트를 비우고 새로운 정보를 추가하여 교체 작업을 수행
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
