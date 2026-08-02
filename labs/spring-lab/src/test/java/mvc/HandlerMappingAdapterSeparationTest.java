package mvc;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HandlerMappingAdapterSeparationTest {

    private static final List<String> EVENTS = new ArrayList<>();

    @BeforeEach
    void clearEvents() {
        EVENTS.clear();
    }

    @Test
    void usesMethodStyleAdapterForMethodStyleHandler() throws Exception {
        ResponseResult result = performRequest("/method-handler");

        assertEquals(
                List.of("handler-mapping", "method-style-adapter", "method-style-handler"),
                EVENTS
        );
        assertEquals("method-ok", result.body());
    }

    @Test
    void usesDirectStyleAdapterForDirectStyleHandler() throws Exception {
        ResponseResult result = performRequest("/direct-handler");

        assertEquals(
                List.of("handler-mapping", "direct-style-adapter", "direct-style-handler"),
                EVENTS
        );
        assertEquals("direct-ok", result.body());
    }

    @Test
    void doesNotInvokeAdapterWhenNoHandlerIsMapped() throws Exception {
        ResponseResult result = performRequest("/missing");

        assertEquals(
                List.of("handler-mapping"),
                EVENTS
        );
        assertEquals(404, result.status());
    }

    private ResponseResult performRequest(String requestUri) throws Exception {
        MockServletContext servletContext = new MockServletContext();

        try (AnnotationConfigWebApplicationContext applicationContext =
                     new AnnotationConfigWebApplicationContext()) {
            applicationContext.setServletContext(servletContext);
            applicationContext.register(TestConfig.class);
            applicationContext.refresh();

            DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
            dispatcherServlet.init(new MockServletConfig(servletContext));

            MockHttpServletRequest request =
                    new MockHttpServletRequest(servletContext, "GET", requestUri);
            MockHttpServletResponse response = new MockHttpServletResponse();

            dispatcherServlet.service(
                    (ServletRequest) request,
                    (ServletResponse) response
            );

            return new ResponseResult(response.getStatus(), response.getContentAsString());
        }
    }

    record ResponseResult(int status, String body) {
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        MethodStyleHandler methodStyleHandler() {
            return new MethodStyleHandler();
        }

        @Bean
        DirectStyleHandler directStyleHandler() {
            return new DirectStyleHandler();
        }

        @Bean
        HandlerMapping pathHandlerMapping(
                MethodStyleHandler methodStyleHandler,
                DirectStyleHandler directStyleHandler
        ) {
            Map<String, Object> handlersByPath = Map.of(
                    "/method-handler", methodStyleHandler,
                    "/direct-handler", directStyleHandler
            );

            return request -> {
                EVENTS.add("handler-mapping");

                Object handler = handlersByPath.get(request.getRequestURI());
                return handler == null ? null : new HandlerExecutionChain(handler);
            };
        }

        @Bean
        HandlerAdapter methodStyleHandlerAdapter() {
            return new HandlerAdapter() {
                @Override
                public boolean supports(Object handler) {
                    return handler instanceof MethodStyleHandler;
                }

                @Override
                public ModelAndView handle(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Object handler
                ) throws IOException {
                    EVENTS.add("method-style-adapter");
                    ((MethodStyleHandler) handler).execute(response);
                    return null;
                }
            };
        }

        @Bean
        HandlerAdapter directStyleHandlerAdapter() {
            return new HandlerAdapter() {
                @Override
                public boolean supports(Object handler) {
                    return handler instanceof DirectStyleHandler;
                }

                @Override
                public ModelAndView handle(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Object handler
                ) throws IOException {
                    EVENTS.add("direct-style-adapter");
                    ((DirectStyleHandler) handler).handle(request, response);
                    return null;
                }
            };
        }
    }

    static class MethodStyleHandler {

        void execute(HttpServletResponse response) throws IOException {
            EVENTS.add("method-style-handler");
            response.setStatus(200);
            response.getWriter().write("method-ok");
        }
    }

    static class DirectStyleHandler {

        void handle(
                HttpServletRequest request,
                HttpServletResponse response
        ) throws IOException {
            EVENTS.add("direct-style-handler");
            response.setStatus(200);
            response.getWriter().write("direct-ok");
        }
    }
}
