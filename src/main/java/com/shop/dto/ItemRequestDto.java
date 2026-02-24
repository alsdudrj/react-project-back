package com.shop.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class ItemRequestDto {
    private String title;
    private int price;
    private String content;
    private String category;
    private String producer;
    private String origin;
    private String shipping;
    private String imgUrl;
    private List<SizeStockDto> sizeStocks;
}
