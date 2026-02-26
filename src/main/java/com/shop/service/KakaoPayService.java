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

@Service
public class KakaoPayService {
    @Value("${react.api.api.url}")
    private String baseUrl;

    @Value("${kakaopay.secret.key}")
    private String adminKey; //Secret Key

    private String tid; //결제 고유 번호

    //결제 준비
    public Map<String, Object> payReady(String itemName, int totalAmount) {
        RestTemplate rt = new RestTemplate();

        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
        String orderId = String.valueOf(System.currentTimeMillis()); //주문번호 생성

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");
        params.put("partner_order_id", orderId);
        params.put("partner_user_id", "gorani");
        params.put("item_name", itemName);
        params.put("quantity", 1);
        params.put("total_amount", totalAmount);
        params.put("tax_free_amount", 0);

        //리액트 서버의 주소 (성공/취소/실패)
        params.put("approval_url", baseUrl + "/payment/success");
        params.put("cancel_url", baseUrl + "/cart");
        params.put("fail_url", baseUrl + "/cart");

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        Map<String, Object> response = rt.postForObject(url, body, Map.class);

        //응답에 주문번호 포함
        if (response != null) {
            response.put("partner_order_id", orderId);
        }
        return response;
    }

    //결제 승인요청
    public Map<String, Object> payApprove(String pgToken, String tid, String partnerOrderId) {
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + adminKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");
        params.put("tid", tid);
        params.put("partner_order_id", partnerOrderId);
        params.put("partner_user_id", "gorani");
        params.put("pg_token", pgToken); // 리액트에서 전달받은 토큰

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

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
