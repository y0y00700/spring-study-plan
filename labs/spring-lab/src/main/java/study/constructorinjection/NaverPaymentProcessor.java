package study.constructorinjection;

public class NaverPaymentProcessor implements PaymentProcessor {

    @Override
    public String pay() {
        return "NAVER";
    }
}