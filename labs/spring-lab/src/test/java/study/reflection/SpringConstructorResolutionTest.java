package study.reflection;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringConstructorResolutionTest {

    private static final List<String> events = new ArrayList<>();

    @Test
    void resolvesConstructorDependencyBeforeInvokingConstructor() {
        events.clear();

        Constructor<?> constructor = OrderService.class.getDeclaredConstructors()[0];
        Class<?> dependencyType = constructor.getParameterTypes()[0];

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext()) {
            context.registerBean(
                    PaymentProcessor.class,
                    beanDefinition -> beanDefinition.setLazyInit(true)
            );
            context.registerBean(OrderService.class);

            context.refresh();

            // TODO: Reflection으로 얻은 생성자 매개변수 타입을 검증하세요.
            assertEquals(PaymentProcessor.class,dependencyType);
            // TODO: 실제 객체 생성 순서를 검증하세요.
            assertEquals(List.of("PaymentProcessor 생성","OrderService 생성"),events);
        }
    }

    static class PaymentProcessor {
        PaymentProcessor() {
            events.add("PaymentProcessor 생성");
        }
    }

    static class OrderService {
        OrderService(PaymentProcessor paymentProcessor) {
            events.add("OrderService 생성");
        }
    }
}
