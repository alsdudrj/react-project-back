package com.shop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 카카오페이 결제 연동 서비스
 * 카카오페이 Open API를 호출하여 결제 준비 및 승인 프로세스를 수행
 */
@Service
public class KakaoPayService {
    @Value("${react.api.api.url}")
    private String baseUrl;

    @Value("${kakaopay.secret.key}")
    private String adminKey; //Secret Key

    private String tid; //결제 고유 번호

    //결제 준비
    //가맹점 정보와 상품 정보를 카카오페이 서버에 전달하고, 결제 고유 번호(TID)와 Redirect URL을 받음
    public Map<String, Object> payReady(String itemName, int totalAmount) {
        RestTemplate rt = new RestTemplate();

        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
        String orderId = String.valueOf(System.currentTimeMillis()); //주문번호 생성 (현재 시간 밀리초를 활용한 고유값)

        //헤더 설정 - 인증 키(Secret Key)와 콘텐츠 타입 지정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //파라미터 설정 (카카오페이 필수 규격)
        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");              // 테스트용 가맹점 코드
        params.put("partner_order_id", orderId);      // 가맹점 주문번호
        params.put("partner_user_id", "gorani");      // 가맹점 회원 ID
        params.put("item_name", itemName);            // 상품명
        params.put("quantity", 1);                    // 상품 수량
        params.put("total_amount", totalAmount);      // 총 금액
        params.put("tax_free_amount", 0);             // 비과세 금액

        //진행별 리다이렉트 리액트 서버의 주소 (성공/취소/실패)
        params.put("approval_url", baseUrl + "/payment/success");
        params.put("cancel_url", baseUrl + "/cart");
        params.put("fail_url", baseUrl + "/cart");

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        //카카오페이 서버로 POST 요청
        Map<String, Object> response = rt.postForObject(url, body, Map.class);

        //응답에 주문번호 포함 (이후 승인 단계에서 필요)
        if (response != null) {
            response.put("partner_order_id", orderId);
        }
        return response;
    }

    //결제 승인요청
    //사용자가 결제 수단을 선택하고 인증을 마친 뒤, 실제로 결제를 확정 짓는 단계
    public Map<String, Object> payApprove(String pgToken, String tid, String partnerOrderId) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");
        params.put("tid", tid);                         // 준비 단계에서 발행된 TID
        params.put("partner_order_id", partnerOrderId); // 준비 단계에서 생성한 주문번호
        params.put("partner_user_id", "gorani");
        params.put("pg_token", pgToken);                // 프론트에서 전달받은 인증 토큰

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        //카카오페이 서버에 최종 승인 요청
        try {
            return rt.postForObject(
                    "https://open-api.kakaopay.com/online/v1/payment/approve",
                    body, Map.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            System.err.println("카카오 승인 에러 상세: " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
