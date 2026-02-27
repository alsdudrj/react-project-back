## 소개
신발 쇼핑몰 웹 서비스의 백엔드 입니다.  
Spring Boot 기반 REST API 서버입니다.

## 기술 스택
- Java
- Spring Boot
- JPA
- Postgres DB (SupaBase)

## 주요 기능
- 회원가입 / 로그인
- 상품 목록 조회
- 상품 상세 페이지
- 장바구니
- 주문 기능
- 관리자 상품 관리 

## 호스팅 주소
- [링크](https://react-project-14mq.vercel.app/)

### Hosting Server
- 백엔드: Render

## 코드 설명
### 로그인
```java
//로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        String token = memberService.login(loginRequest);

        return ResponseEntity.ok(token);
    }
    
    //소셜로그인
    @PostMapping("/social-login")
    public ResponseEntity<?> socialLogin(@RequestBody SocialLoginRequest request) {
        //소셜 계정으로 로그인 또는 회원가입 처리 후 토큰 생성
        String token = memberService.processSocialLogin(request);

        //서버전용 JWT 토큰 반환
        return ResponseEntity.ok(token);
    }
```

## 실행 방법
.env 생성 후 application.properties 변수 설정 
서버 실행
