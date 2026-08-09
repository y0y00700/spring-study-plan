package aop;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdkDynamicProxyCglibTest {

    @Test
    void jdkDynamicProxyRuntimeTypeAndDelegation() {
        List<String> events = new ArrayList<>();
        PaymentServiceImpl target = new PaymentServiceImpl(events);
        Object proxy = createJdkProxy(target, events);

        Boolean expectedSameReference = false; // TODO: true 또는 false
        Boolean expectedPaymentServiceType = true; // TODO: true 또는 false
        Boolean expectedImplementationType = false; // TODO: true 또는 false
        Boolean expectedSameRuntimeClass = false; // TODO: true 또는 false
        List<String> expectedEvents = List.of("advice-before","target","advice-after-returning","advice-finally"); // TODO: 예상 호출 순서

        assertEquals(expectedSameReference, proxy == target);
        assertEquals(
                expectedPaymentServiceType,
                proxy instanceof PaymentService
        );
        assertEquals(
                expectedImplementationType,
                proxy instanceof PaymentServiceImpl
        );
        assertEquals(
                expectedSameRuntimeClass,
                proxy.getClass() == target.getClass()
        );

        String result = ((PaymentService) proxy).pay();

        assertEquals("paid", result);
        assertEquals(expectedEvents, events);
    }

    @Test
    void cglibProxyRuntimeTypeAndDelegation() {
        List<String> events = new ArrayList<>();
        PaymentServiceImpl target = new PaymentServiceImpl(events);
        Object proxy = createCglibProxy(target, events);

        Boolean expectedSameReference = false; // TODO: true 또는 false
        Boolean expectedPaymentServiceType = true; // TODO: true 또는 false
        Boolean expectedImplementationType = true; // TODO: true 또는 false
        Boolean expectedSameRuntimeClass = false; // TODO: true 또는 false
        List<String> expectedEvents = List.of("advice-before","target","advice-after-returning","advice-finally"); // TODO: 예상 호출 순서

        assertEquals(expectedSameReference, proxy == target);
        assertEquals(
                expectedPaymentServiceType,
                proxy instanceof PaymentService
        );
        assertEquals(
                expectedImplementationType,
                proxy instanceof PaymentServiceImpl
        );
        assertEquals(
                expectedSameRuntimeClass,
                proxy.getClass() == target.getClass()
        );

        String result = ((PaymentService) proxy).pay();

        assertEquals("paid", result);
        assertEquals(expectedEvents, events);
    }

    @Test
    void concreteClassCastDependsOnProxyStrategy() {
        List<String> events = new ArrayList<>();
        PaymentServiceImpl target = new PaymentServiceImpl(events);
        Object jdkProxy = createJdkProxy(target, events);
        Object cglibProxy = createCglibProxy(target, events);

        Class<? extends Throwable> expectedJdkCastFailure = ClassCastException.class;
        // TODO: JDK 프록시를 구현 클래스 타입으로 캐스팅할 때의 예외 타입
        Boolean expectedCglibCastKeepsSameProxyReference = true;
        // TODO: true 또는 false

        assertThrows(
                expectedJdkCastFailure,
                () -> {
                    PaymentServiceImpl ignored =
                            (PaymentServiceImpl) jdkProxy;
                }
        );

        PaymentServiceImpl concreteTypedCglibProxy =
                (PaymentServiceImpl) cglibProxy;
        assertEquals(
                expectedCglibCastKeepsSameProxyReference,
                concreteTypedCglibProxy == cglibProxy
        );
    }

    @Test
    void cglibProxyCreationFailsForFinalClass() {
        FinalPaymentService target = new FinalPaymentService();

        Boolean expectedProxyCreationFailure = true;
        // TODO: CGLIB 프록시 생성이 실패할지 true 또는 false로 예측

        Throwable actualFailure = null;
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setTarget(target);
            factory.setProxyTargetClass(true);
            factory.getProxy();
        } catch (Throwable throwable) {
            actualFailure = throwable;
        }

        assertEquals(
                expectedProxyCreationFailure,
                actualFailure != null
        );
    }

    @Test
    void cglibProxyCannotInterceptFinalMethod() {
        List<String> events = new ArrayList<>();
        FinalMethodPaymentService target =
                new FinalMethodPaymentService();

        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(recordingAdvice(events));

        FinalMethodPaymentService proxy =
                (FinalMethodPaymentService) factory.getProxy();

        Boolean expectedAdviceApplied = false;
        // TODO: final 메서드 호출에 Advice가 적용될지 true 또는 false

        String result = proxy.pay();

        assertEquals("paid", result);
        assertEquals(
                expectedAdviceApplied,
                events.contains("advice-before")
        );
    }

    @Test
    void adviceCanReturnWithoutCallingTarget() {
        List<String> events = new ArrayList<>();
        PaymentServiceImpl target = new PaymentServiceImpl(events);

        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.setInterfaces(PaymentService.class);
        factory.setProxyTargetClass(false);
        factory.addAdvice((MethodInterceptor) invocation -> {
            events.add("advice-short-circuit");
            return "blocked";
        });

        PaymentService proxy = (PaymentService) factory.getProxy();

        String expectedResult = "blocked";
        // TODO: "paid" 또는 "blocked"
        Boolean expectedTargetCalled = false;
        // TODO: target의 pay()가 호출되면 true, 아니면 false

        String actualResult = proxy.pay();

        assertEquals(expectedResult, actualResult);
        assertEquals(
                expectedTargetCalled,
                events.contains("target")
        );
    }

    private Object createJdkProxy(
            PaymentService target,
            List<String> events
    ) {
        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.setInterfaces(PaymentService.class);
        factory.setProxyTargetClass(false);
        factory.addAdvice(recordingAdvice(events));
        return factory.getProxy();
    }

    private Object createCglibProxy(
            PaymentServiceImpl target,
            List<String> events
    ) {
        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(recordingAdvice(events));
        return factory.getProxy();
    }

    private MethodInterceptor recordingAdvice(List<String> events) {
        return invocation -> {
            events.add("advice-before");
            try {
                Object result = invocation.proceed();
                events.add("advice-after-returning");
                return result;
            } finally {
                events.add("advice-finally");
            }
        };
    }

    interface PaymentService {
        String pay();
    }

    static class PaymentServiceImpl implements PaymentService {

        private final List<String> events;

        PaymentServiceImpl(List<String> events) {
            this.events = events;
        }

        @Override
        public String pay() {
            events.add("target");
            return "paid";
        }
    }

    static final class FinalPaymentService {

        String pay() {
            return "paid";
        }
    }

    static class FinalMethodPaymentService {

        public final String pay() {
            return "paid";
        }
    }
}
