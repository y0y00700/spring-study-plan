package com.study.springlab.container;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContainerLifecycleIntegrationTest {

    private static final List<String> events = new ArrayList<>();

    @BeforeEach
    void resetEvents() {
        events.clear();
    }

    @Test
    void beanTravelsFromDefinitionRegistrationToDestruction() {
        var context =
                new AnnotationConfigApplicationContext(TestConfig.class);

        try {
            // TODO 1: reportService BeanDefinition 등록 여부 검증
            assertTrue(context.containsBeanDefinition("reportService"));
            // TODO 2: 컨텍스트 초기화 직후 이벤트 순서 검증
            assertEquals(List.of("repository.constructor","service.constructor","service.postConstruct"),events);
            var eventsAfterInitialization = List.copyOf(events);
            var service1 = context.getBean(ReportService.class);
            var service2 = context.getBean(ReportService.class);
            var repository = context.getBean(ReportRepository.class);

            // TODO 3: 두 ReportService의 참조 동일성 검증
            assertSame(service1,service2);
            // TODO 4: 주입된 repository와 조회한 repository의 참조 동일성 검증
            assertSame(repository,service1.repository);
            assertSame(repository,service2.repository);
            // TODO 5: getBean()이 반환한 객체의 초기화 완료 상태 검증
            assertTrue(service1.initialized);
            assertTrue(service2.initialized);
            // TODO 6: getBean() 이후 새 생성·초기화 이벤트가 없는지 검증
            //모르겠음
            assertEquals(eventsAfterInitialization,events);
        } finally {
            context.close();
        }

        // TODO 7: close 이후 소멸 콜백까지 포함한 전체 이벤트 순서 검증
        assertEquals(List.of("repository.constructor","service.constructor","service.postConstruct","service.preDestroy"),events);
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        ReportRepository reportRepository() {
            return new ReportRepository();
        }

        @Bean
        ReportService reportService(ReportRepository repository) {
            return new ReportService(repository);
        }
    }

    static class ReportRepository {

        ReportRepository() {
            events.add("repository.constructor");
        }
    }

    static class ReportService {

        private final ReportRepository repository;
        private boolean initialized;

        ReportService(ReportRepository repository) {
            this.repository = repository;
            events.add("service.constructor");
        }

        @PostConstruct
        void initialize() {
            initialized = true;
            events.add("service.postConstruct");
        }

        @PreDestroy
        void destroy() {
            events.add("service.preDestroy");
        }
    }
}