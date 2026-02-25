package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {
    private String userName;
    private String email;
    private String displayName;
    private String socialType;
}
