package study.constructorinjection;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class QualifierPaymentBeanConfig {

    @Bean
    @Primary
    PaymentProcessor kakaoProcessor() {
        return new KakaoPaymentProcessor();
    }

    @Bean
    PaymentProcessor naverProcessor() {
        return new NaverPaymentProcessor();
    }

    @Bean
    OrderService orderService(@Qualifier("naverProcessor") PaymentProcessor paymentProcessor) {
        return new OrderService(paymentProcessor);
    }
}