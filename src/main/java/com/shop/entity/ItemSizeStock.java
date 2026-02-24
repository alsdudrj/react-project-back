package com.shop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ItemSizeStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String size;
    private int stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    @JsonIgnore
    private Item item;
}
