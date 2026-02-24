package com.shop.entity;

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
}
