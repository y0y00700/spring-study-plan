package mvc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = FilterInterceptorAopBoundaryTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class FilterInterceptorAopBoundaryTest {

    private static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    @LocalServerPort
    int port;

    HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void clearEvents() {
        EVENTS.clear();
    }

    @Test
    void normalRequestShowsAllThreeBoundaries() throws Exception {
        HttpResponse<String> response = get("/mvc-05/flow");

        assertEquals(200, response.statusCode());

        List<String> expectedEvents = List.of(
                "filter-before","interceptor-preHandle","aop-before","controller","aop-after-returning","aop-finally","interceptor-postHandle","interceptor-afterCompletion:null","filter-after"
        );
        assertEquals(expectedEvents, EVENTS);
    }

    @Test
    void handledControllerExceptionShowsExceptionResolutionBoundary()
            throws Exception {
        HttpResponse<String> response = get("/mvc-05/flow?controllerFailure=true");

        assertEquals(409, response.statusCode());

        List<String> expectedEvents = List.of(
                "filter-before","interceptor-preHandle","aop-before","controller","aop-after-throwing","aop-finally","advice-sold-out","interceptor-afterCompletion:null","filter-after"
        );
        assertEquals(expectedEvents, EVENTS);
    }

    @Test
    void filterExceptionDoesNotReachControllerAdvice() throws Exception {
        HttpResponse<String> response = get("/mvc-05/flow?filterFailure=true");

        assertEquals(500, response.statusCode());

        List<String> expectedEvents = List.of(
                "filter-before","filter-throwing"
        );
        assertEquals(expectedEvents, EVENTS);
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableAspectJAutoProxy
    @Import({
            ControllerCallAspect.class,
            GlobalExceptionHandler.class,
            InterceptorConfiguration.class,
            FilterConfiguration.class
    })
    static class TestApplication {

        @Bean
        FlowController flowController() {
            return new FlowController();
        }
    }

    @RestController
    static class FlowController {

        @GetMapping("/mvc-05/flow")
        String flow(
                @RequestParam(
                        name = "controllerFailure",
                        defaultValue = "false"
                ) boolean controllerFailure
        ) {
            EVENTS.add("controller");

            if (controllerFailure) {
                throw new SoldOutException();
            }

            return "ok";
        }
    }

    @Aspect
    static class ControllerCallAspect {

        @Around("bean(flowController)")
        Object recordControllerCall(ProceedingJoinPoint joinPoint)
                throws Throwable {
            EVENTS.add("aop-before");

            try {
                Object result = joinPoint.proceed();
                EVENTS.add("aop-after-returning");
                return result;
            } catch (Throwable throwable) {
                EVENTS.add("aop-after-throwing");
                throw throwable;
            } finally {
                EVENTS.add("aop-finally");
            }
        }
    }

    static class FlowInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(
                HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                Object handler
        ) {
            EVENTS.add("interceptor-preHandle");
            return true;
        }

        @Override
        public void postHandle(
                HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                Object handler,
                ModelAndView modelAndView
        ) {
            EVENTS.add("interceptor-postHandle");
        }

        @Override
        public void afterCompletion(
                HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                Object handler,
                Exception exception
        ) {
            String exceptionName = exception == null
                    ? "null"
                    : exception.getClass().getSimpleName();
            EVENTS.add("interceptor-afterCompletion:" + exceptionName);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class InterceptorConfiguration implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new FlowInterceptor())
                    .addPathPatterns("/mvc-05/**");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class FilterConfiguration {

        @Bean
        FilterRegistrationBean<Filter> flowBoundaryFilter() {
            Filter filter = new Filter() {
                @Override
                public void doFilter(
                        ServletRequest request,
                        ServletResponse response,
                        FilterChain chain
                ) throws IOException, ServletException {
                    EVENTS.add("filter-before");

                    HttpServletRequest httpRequest =
                            (HttpServletRequest) request;
                    if ("true".equals(
                            httpRequest.getParameter("filterFailure")
                    )) {
                        EVENTS.add("filter-throwing");
                        throw new FilterFailureException();
                    }

                    chain.doFilter(request, response);
                    EVENTS.add("filter-after");
                }
            };

            FilterRegistrationBean<Filter> registration =
                    new FilterRegistrationBean<>(filter);
            registration.addUrlPatterns("/mvc-05/*");
            return registration;
        }
    }

    @RestControllerAdvice
    static class GlobalExceptionHandler {

        @ExceptionHandler(SoldOutException.class)
        ResponseEntity<ApiError> handleSoldOut(SoldOutException exception) {
            EVENTS.add("advice-sold-out");
            return ResponseEntity
                    .status(409)
                    .body(new ApiError("SOLD_OUT"));
        }

        @ExceptionHandler(FilterFailureException.class)
        ResponseEntity<ApiError> handleFilterFailure(
                FilterFailureException exception
        ) {
            EVENTS.add("advice-filter-failure");
            return ResponseEntity
                    .status(500)
                    .body(new ApiError("FILTER_FAILURE"));
        }
    }

    record ApiError(String code) {
    }

    static class SoldOutException extends RuntimeException {
    }

    static class FilterFailureException extends ServletException {
    }
}
