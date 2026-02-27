package com.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MemberResponseDTO {
    private Long id;
    private String userName;
    private String email;
    private String address;
    private String detailAddress;
}
