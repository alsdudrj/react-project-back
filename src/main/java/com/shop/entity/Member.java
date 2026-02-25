package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;

    private String password;

    private String displayName;

    private String email;

    private String address;

    private String detailAddress;

    @Enumerated(EnumType.STRING)
    private Role auth;
}
