package com.study.springlab.container;

import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BeanDestructionScopeTest {

    private static final AtomicInteger singletonCreated = new AtomicInteger();
    private static final AtomicInteger singletonDestroyed = new AtomicInteger();
    private static final AtomicInteger prototypeCreated = new AtomicInteger();
    private static final AtomicInteger prototypeDestroyed = new AtomicInteger();

    @BeforeEach
    void resetCounters() {
        singletonCreated.set(0);
        singletonDestroyed.set(0);
        prototypeCreated.set(0);
        prototypeDestroyed.set(0);
    }

    @Test
    void contextCloseInvokesSingletonDestroyButNotPrototypeDestroy() {
        var context =
                new AnnotationConfigApplicationContext(TestConfig.class);

        // 명시적인 getBean() 호출 전 생성 상태
        assertEquals(1, singletonCreated.get());
        assertEquals(0, prototypeCreated.get());
        
        var singleton1 = context.getBean(SingletonBean.class);
        var singleton2 = context.getBean(SingletonBean.class);
        var prototype1 = context.getBean(PrototypeBean.class);
        var prototype2 = context.getBean(PrototypeBean.class);

        // TODO 1: singleton1과 singleton2의 동일성 검증
        assertSame(singleton1 , singleton2);
        // TODO 2: prototype1과 prototype2의 동일성 검증
        assertNotSame(prototype1,prototype2);
        // TODO 3: close 전 Singleton 생성 횟수 검증
        assertEquals(1,singletonCreated.get());
        // TODO 4: close 전 Prototype 생성 횟수 검증
        assertEquals(2,prototypeCreated.get());
        context.close();

        // TODO 5: close 후 Singleton 소멸 횟수 검증
        assertEquals(1,singletonDestroyed.get());
        // TODO 6: close 후 Prototype 소멸 횟수 검증
        assertEquals(0,prototypeDestroyed.get());
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        SingletonBean singletonBean() {
            return new SingletonBean();
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        PrototypeBean prototypeBean() {
            return new PrototypeBean();
        }
    }

    static class SingletonBean {

        SingletonBean() {
            singletonCreated.incrementAndGet();
        }

        @PreDestroy
        void destroy() {
            singletonDestroyed.incrementAndGet();
        }
    }

    static class PrototypeBean {

        PrototypeBean() {
            prototypeCreated.incrementAndGet();
        }

        @PreDestroy
        void destroy() {
            prototypeDestroyed.incrementAndGet();
        }
    }
}