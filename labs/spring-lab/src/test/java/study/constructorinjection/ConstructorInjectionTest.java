package study.constructorinjection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstructorInjectionTest {

    @Test
    void keepsInjectedProcessorWhenLocalVariableIsReassigned() {
        PaymentProcessor processor = new KakaoPaymentProcessor();
        OrderService orderService = new OrderService(processor);

        processor = new NaverPaymentProcessor();

        // 여기에 orderService.pay()의 결과를 검증하는 코드를 직접 작성
        // 첫번째 인수는 Expect / 두번째는 actual
        assertEquals("KAKAO", orderService.pay());
        assertEquals("NAVER", orderService.pay());

    }
    @Test
    void usesProcessorProvidedAtConstructionTime() {
        PaymentProcessor kakaoProcessor = new KakaoPaymentProcessor();
        PaymentProcessor naverProcessor = new NaverPaymentProcessor();

        OrderService kakaoOrderService = new OrderService(kakaoProcessor);
        OrderService naverOrderService = new OrderService(naverProcessor);

        // 두 OrderService의 pay() 결과를 각각 검증
        assertEquals("KAKAO",kakaoOrderService.pay());
        assertEquals("NAVER",naverOrderService.pay());
    }
}