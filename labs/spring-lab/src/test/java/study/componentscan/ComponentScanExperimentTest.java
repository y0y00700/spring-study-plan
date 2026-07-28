package study.componentscan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentScanExperimentTest {

    private static final Map<String, Integer> constructorCalls = new HashMap<>();

    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface IncludedInScan {
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    public @interface ExcludedFromScan {
    }

    public static void recordConstruction(String name) {
        constructorCalls.merge(name, 1, Integer::sum);
    }

    private static int constructionCount(String name) {
        return constructorCalls.getOrDefault(name, 0);
    }

    @BeforeEach
    void resetConstructionCounts() {
        constructorCalls.clear();
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(
            basePackages = "study.componentscan.inside",
            lazyInit = true,
            includeFilters = @ComponentScan.Filter(
                    type = FilterType.ANNOTATION,
                    classes = IncludedInScan.class
            ),
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ANNOTATION,
                    classes = ExcludedFromScan.class
            )
    )
    static class ScanConfig {
    }

    @Test
    void scanRangeAndFiltersAreAppliedBeforeInstantiation() {
        try (var context =
                     new AnnotationConfigApplicationContext(ScanConfig.class)) {

            // TODO 1: 네 클래스의 BeanDefinition 등록 여부를 검증하세요.
            // orderService
            // specialService
            // legacyService
            // externalService
            // externalSpecialService
            assertTrue(context.containsBeanDefinition("orderService"));
            assertTrue(context.containsBeanDefinition("specialService"));
            assertFalse(context.containsBeanDefinition("legacyService"));
            assertFalse(context.containsBeanDefinition("externalService"));
            assertFalse(context.containsBeanDefinition("externalSpecialService"));

            // TODO 2: getBean() 전 orderService 생성 횟수가 0인지 검증하세요.
            assertEquals(0,constructionCount("orderService"));
            assertEquals(0,constructionCount("specialService"));
            assertEquals(0,constructionCount("legacyService"));
            assertEquals(0,constructionCount("externalService"));
            assertEquals(0,constructionCount("externalSpecialService"));
            context.getBean("orderService");

            // TODO 3: getBean() 후 orderService 생성 횟수가 1인지 검증하세요.
            assertEquals(1,constructionCount("orderService"));
            assertEquals(0,constructionCount("specialService"));
            assertEquals(0,constructionCount("legacyService"));
            assertEquals(0,constructionCount("externalService"));
            assertEquals(0,constructionCount("externalSpecialService"));
        }
    }
}