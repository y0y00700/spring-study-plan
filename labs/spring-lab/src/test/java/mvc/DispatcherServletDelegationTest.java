package mvc;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DispatcherServletDelegationTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @Test
    void dispatcherDelegatesHandlerLookupAndInvocation() throws Exception {
        MockServletContext servletContext = new MockServletContext();

        try (AnnotationConfigWebApplicationContext applicationContext =
                     new AnnotationConfigWebApplicationContext()) {
            applicationContext.setServletContext(servletContext);
            applicationContext.register(TestConfig.class);
            applicationContext.refresh();

            DispatcherServlet dispatcherServlet =
                    new RecordingDispatcherServlet(applicationContext);
            dispatcherServlet.init(new MockServletConfig(servletContext));

            EVENTS.clear();

            MockHttpServletRequest request =
                    new MockHttpServletRequest(servletContext, "GET", "/orders/1");
            MockHttpServletResponse response = new MockHttpServletResponse();

            dispatcherServlet.service(
                    (ServletRequest) request,
                    (ServletResponse) response
            );

            assertEquals(200, response.getStatus());
            assertEquals("order-1", response.getContentAsString());

            assertEquals(
                    List.of(
                            "dispatcher-servlet","handler-mapping","handler-adapter","controller"
                    ),
                    EVENTS
            );
        }
    }

    static class RecordingDispatcherServlet extends DispatcherServlet {

        RecordingDispatcherServlet(
                AnnotationConfigWebApplicationContext applicationContext
        ) {
            super(applicationContext);
        }

        @Override
        protected void doService(
                HttpServletRequest request,
                HttpServletResponse response
        ) throws Exception {
            EVENTS.add("dispatcher-servlet");
            super.doService(request, response);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        TestController testController() {
            return new TestController();
        }

        @Bean
        HandlerMapping studyHandlerMapping(TestController controller) {
            return request -> {
                EVENTS.add("handler-mapping");

                if (!"/orders/1".equals(request.getRequestURI())) {
                    return null;
                }

                return new HandlerExecutionChain(controller);
            };
        }

        @Bean
        HandlerAdapter studyHandlerAdapter() {
            return new HandlerAdapter() {
                @Override
                public boolean supports(Object handler) {
                    return handler instanceof TestController;
                }

                @Override
                public ModelAndView handle(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Object handler
                ) throws IOException {
                    EVENTS.add("handler-adapter");
                    ((TestController) handler).handle(response);
                    return null;
                }

            };
        }
    }

    static class TestController {

        void handle(HttpServletResponse response) throws IOException {
            EVENTS.add("controller");
            response.setStatus(200);
            response.getWriter().write("order-1");
        }
    }
}
