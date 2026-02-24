package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequest {

    private String userName;

    private String password;

    private String displayName;

    private String address;

    private String detailAddress;

    private String auth;
}
