package study.lifecycle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeanLifecycleOrderTest {

    @Test
    void beanPostProcessorWrapsInitializationCallback() {
        List<String> events = new ArrayList<>();

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext()) {

            context.getBeanFactory().addBeanPostProcessor(
                    new RecordingBeanPostProcessor(events)
            );

            context.registerBean(
                    "lifecycleBean",
                    LifecycleBean.class,
                    () -> new LifecycleBean(events)
            );

            context.refresh();

            List<String> expected = List.of(
                    // 여기에 방금 예측한 네 이벤트를 순서대로 작성
                    "constructor","setBeanName","beforeInitialization","postConstruct","afterPropertiesSet","afterInitialization"
            );

            assertEquals(expected, events);
        }
    }

    static class LifecycleBean implements BeanNameAware, InitializingBean {

        private final List<String> events;

        LifecycleBean(List<String> events) {
            this.events = events;
            events.add("constructor");
        }

        @Override
        public void setBeanName(String name) {
            events.add("setBeanName");
        }

        @Override
        public void afterPropertiesSet() {
            events.add("afterPropertiesSet");
        }

        @PostConstruct
        void postConstruct() {
            events.add("postConstruct");
        }
    }

    static class RecordingBeanPostProcessor implements BeanPostProcessor {

        private final List<String> events;

        RecordingBeanPostProcessor(List<String> events) {
            this.events = events;
        }

        @Override
        public Object postProcessBeforeInitialization(
                Object bean,
                String beanName
        ) {
            if (beanName.equals("lifecycleBean")) {
                events.add("beforeInitialization");
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(
                Object bean,
                String beanName
        ) {
            if (beanName.equals("lifecycleBean")) {
                events.add("afterInitialization");
            }
            return bean;
        }
    }

    @Test
    void postProcessorReturnValueBecomesBeanExposedByContext() {
        GreetingService rawBean = new GreetingService();

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext()) {

            context.getBeanFactory().addBeanPostProcessor(
                    new ProxyingBeanPostProcessor()
            );

            context.registerBean(
                    "greetingService",
                    GreetingService.class,
                    () -> rawBean
            );

            context.refresh();

            Object exposedBean = context.getBean("greetingService");

            assertNotSame(rawBean, exposedBean);
            assertInstanceOf(GreetingServiceProxy.class, exposedBean);
            assertEquals(
                    "proxy -> target",
                    ((Greeting) exposedBean).greet()
            );
        }
    }

    interface Greeting {
        String greet();
    }

    static class GreetingService implements Greeting {

        @Override
        public String greet() {
            return "target";
        }
    }

    static class GreetingServiceProxy implements Greeting {

        private final Greeting target;

        GreetingServiceProxy(Greeting target) {
            this.target = target;
        }

        @Override
        public String greet() {
            return "proxy -> " + target.greet();
        }
    }

    static class ProxyingBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(
                Object bean,
                String beanName
        ) {
            if (beanName.equals("greetingService")) {
                return new GreetingServiceProxy((Greeting) bean);
            }

            return bean;
        }
    }
}

