package com.shop.controller;

import com.shop.service.KakaoPayService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    //결제 준비 요청
    @PostMapping("/ready")
    public Map<String, Object> ready(@RequestBody Map<String, Object> params, HttpSession session) {
        String itemName = params.get("itemName").toString();
        int totalPrice = Integer.parseInt(params.get("totalPrice").toString());

        Map<String, Object> res = kakaoPayService.payReady(itemName, totalPrice);

        //승인 단계에서 사용할 tid를 세션에 저장
        session.setAttribute("tid", res.get("tid").toString());

        return res;
    }

    //결제 승인 요청
    @PostMapping("/approve")
    public Map<String, Object> approve(@RequestBody Map<String, String> map) {
        String pgToken = map.get("pgToken");
        String tid = map.get("tid");
        String partnerOrderId = map.get("partnerOrderId");

        return kakaoPayService.payApprove(pgToken, tid, partnerOrderId);
    }
}
