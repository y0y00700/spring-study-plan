package study.constructorinjection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class LazyPaymentBeanConfig {

    static int processorCreationCount = 0;

    @Bean
    @Lazy
    PaymentProcessor kakaoProcessor() {
        processorCreationCount++;
        return new KakaoPaymentProcessor();
    }

    @Bean
    @Lazy
    PaymentProcessor naverProcessor() {
        processorCreationCount++;
        return new NaverPaymentProcessor();
    }

    @Bean
    OrderService orderService(PaymentProcessor paymentProcessor) {
        return new OrderService(paymentProcessor);
    }
}
