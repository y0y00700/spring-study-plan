package study.constructorinjection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class SpringBeanSelectionTest {
//    @Test
//    void failsWhenMultiplePaymentProcessorsExistWithoutSelectionRule(){
//        new AnnotationConfigApplicationContext(PaymentBeanConfig.class);
//    }

    @Test
    void failsWhenMultiplePaymentProcessorsExistWithoutSelectionRule() {
        UnsatisfiedDependencyException exception = assertThrows(
                UnsatisfiedDependencyException.class,
                () -> new AnnotationConfigApplicationContext(PaymentBeanConfig.class)
        );

        assertInstanceOf(
                NoUniqueBeanDefinitionException.class,
                exception.getCause()
        );

    }

    @Test
    void injectsPrimaryProcessorWhenMultipleCandidatesExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(PrimaryPaymentBeanConfig.class)) {

            OrderService orderService = context.getBean(OrderService.class);

            // pay() 결과가 KAKAO인지 직접 검증
            assertEquals("KAKAO",orderService.pay());
        }
    }
    @Test
    void qualifierSelectsNaverEvenWhenKakaoIsPrimary() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(QualifierPaymentBeanConfig.class)) {

            OrderService orderService = context.getBean(OrderService.class);

            // NAVER를 검증
            assertEquals("NAVER",orderService.pay());
        }
    }
    @Test
    void doesNotInstantiateLazyCandidatesBeforeAmbiguityIsDetected(){
        LazyPaymentBeanConfig.processorCreationCount = 0;

        UnsatisfiedDependencyException exception = assertThrows(
                UnsatisfiedDependencyException.class,
                () -> new AnnotationConfigApplicationContext(
                        LazyPaymentBeanConfig.class
                )
        );

        assertInstanceOf(
                NoUniqueBeanDefinitionException.class,
                exception.getCause()
        );

        assertEquals(0, LazyPaymentBeanConfig.processorCreationCount);
    }

}
