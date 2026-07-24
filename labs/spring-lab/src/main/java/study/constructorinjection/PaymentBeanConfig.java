package study.constructorinjection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class PaymentBeanConfig {

    @Bean
    PaymentProcessor kakaoProcessor() {
        return new KakaoPaymentProcessor();
    }

    @Bean
    PaymentProcessor naverProcessor() {
        return new NaverPaymentProcessor();
    }

    @Bean
    OrderService orderService(PaymentProcessor paymentProcessor) {
        return new OrderService(paymentProcessor);
    }
}