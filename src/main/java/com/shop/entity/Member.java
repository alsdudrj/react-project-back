package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder    //빌더 패턴을 사용하여 객체 생성을 유연하게 함
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
