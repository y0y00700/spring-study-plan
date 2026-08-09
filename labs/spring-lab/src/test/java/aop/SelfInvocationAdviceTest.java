package aop;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelfInvocationAdviceTest {

    @Test
    void externalInnerCallPassesProxy() {
        List<String> events = new ArrayList<>();
        CallService target = new CallService(events);
        CallService proxy = createProxy(target, events);

        List<String> expectedEvents = List.of(
                "advice-before","target-inner","advice-after"
        );

        proxy.inner();

        assertEquals(expectedEvents, events);
    }

    @Test
    void outerCallsInnerOnSameObject() {
        List<String> events = new ArrayList<>();
        CallService target = new CallService(events);
        CallService proxy = createProxy(target, events);

        List<String> expectedEvents = List.of(
                "target-outer","target-inner"
        );

        proxy.outer();

        assertEquals(expectedEvents, events);
    }

    private CallService createProxy(
            CallService target,
            List<String> events
    ) {
        NameMatchMethodPointcutAdvisor advisor =
                new NameMatchMethodPointcutAdvisor(
                        recordingAdvice(events)
                );
        advisor.setMappedName("inner");

        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(target);
        factory.setProxyTargetClass(true);
        factory.addAdvisor(advisor);

        return (CallService) factory.getProxy();
    }

    private MethodInterceptor recordingAdvice(List<String> events) {
        return invocation -> {
            events.add("advice-before");
            Object result = invocation.proceed();
            events.add("advice-after");
            return result;
        };
    }

    static class CallService {

        private final List<String> events;

        CallService(List<String> events) {
            this.events = events;
        }

        public void outer() {
            events.add("target-outer");
            inner();
        }

        public void inner() {
            events.add("target-inner");
        }
    }
}
