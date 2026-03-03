package com.shop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemSizeStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String size;
    private int stock;

    /**
     * 상품(Item)과의 다대일(N:1) 연관관계 설정
     * * fetch = FetchType.LAZY: 지연 로딩 설정
     * 실제 item 객체를 사용할 때만 DB에서 조회하여 성능을 최적화
     * * @JoinColumn(name = "item_id"): DB 테이블에서 외래키(FK) 컬럼명을 'item_id'로 지정
     * * @JsonIgnore: JSON 변환 시 이 필드는 제외
     * Item 조회 시 자식(Size)을 부르고, 자식이 다시 부모(Item)를 부르는 '무한 순환 참조'를 방지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private Item item;
}
