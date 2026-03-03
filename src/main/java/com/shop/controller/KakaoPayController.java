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

/**
 * 카카오페이 결제 프로세스 컨트롤러
 * 결제 준비 요청과 최종 승인 요청을 처리
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    //결제 준비 요청
    //사용자가 결제 버튼을 눌렀을 때 카카오페이 서버에 결제 정보를 보내고
    //결제 고유 번호(TID)와 리다이렉트 URL을 받아옴
    @PostMapping("/ready")
    public Map<String, Object> ready(@RequestBody Map<String, Object> params, HttpSession session) {
        //요청 파라미터 추출
        String itemName = params.get("itemName").toString();
        int totalPrice = Integer.parseInt(params.get("totalPrice").toString());

        //카카오페이 서버와 통신
        Map<String, Object> res = kakaoPayService.payReady(itemName, totalPrice);

        //결제 승인 단계에서 사용할 TID(Transaction ID)를 세션에 저장
        session.setAttribute("tid", res.get("tid").toString());

        return res;
    }

    //결제 승인 요청
    //사용자가 카카오톡에서 결제를 인증한 후, 발행된 pgToken을 사용하여 최종 결제 완료 처리
    @PostMapping("/approve")
    public Map<String, Object> approve(@RequestBody Map<String, String> map) {
        //프론트엔드 또는 세션에서 전달받은 필수값들을 추출
        String pgToken = map.get("pgToken");
        String tid = map.get("tid");
        String partnerOrderId = map.get("partnerOrderId");

        //카카오페이 서버에 최종 승인 API 요청 후 결과 반환
        return kakaoPayService.payApprove(pgToken, tid, partnerOrderId);
    }
}
