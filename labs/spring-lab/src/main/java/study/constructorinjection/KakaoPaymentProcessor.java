package study.constructorinjection;

public class KakaoPaymentProcessor implements PaymentProcessor {
    @Override
    public String pay() {
        return "KAKAO";
    }
}
