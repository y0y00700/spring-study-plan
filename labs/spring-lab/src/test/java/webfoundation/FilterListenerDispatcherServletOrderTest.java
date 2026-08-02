package webfoundation;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        classes = FilterListenerDispatcherServletOrderTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class FilterListenerDispatcherServletOrderTest {

    private static final List<String> EVENTS = new CopyOnWriteArrayList<>();

    @Value("${local.server.port}")
    int port;

    @BeforeEach
    void clearEvents() {
        EVENTS.clear();
    }

    @Test
    void requestPassesThroughFilterToController() throws Exception {
        HttpResponse<String> response = sendRequest("/flow");

        assertEquals(200, response.statusCode());

        assertEquals(
                List.of(
                        "request-initialized",
                        "filter-before",
                        "controller",
                        "filter-after",
                        "request-destroyed"
                ),
                EVENTS
        );
    }

    @Test
    void filterCanStopRequestBeforeController() throws Exception {
        HttpResponse<String> response = sendRequest("/flow?blocked=true");

        assertEquals(401, response.statusCode());

        assertEquals(
                List.of(
                        "request-initialized",
                        "filter-before",
                        "filter-blocked",
                        "request-destroyed"
                ),
                EVENTS
        );
    }

    private HttpResponse<String> sendRequest(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        ServletListenerRegistrationBean<ServletRequestListener> requestListener() {
            ServletRequestListener listener = new ServletRequestListener() {
                @Override
                public void requestInitialized(ServletRequestEvent event) {
                    EVENTS.add("request-initialized");
                }

                @Override
                public void requestDestroyed(ServletRequestEvent event) {
                    EVENTS.add("request-destroyed");
                }
            };

            return new ServletListenerRegistrationBean<>(listener);
        }

        @Bean
        FilterRegistrationBean<Filter> flowFilter() {
            Filter filter = new Filter() {
                @Override
                public void doFilter(
                        ServletRequest request,
                        ServletResponse response,
                        FilterChain chain
                ) throws IOException, ServletException {
                    EVENTS.add("filter-before");

                    HttpServletRequest httpRequest = (HttpServletRequest) request;
                    if ("true".equals(httpRequest.getParameter("blocked"))) {
                        EVENTS.add("filter-blocked");
                        ((HttpServletResponse) response).setStatus(401);
                        return;
                    }

                    chain.doFilter(request, response);
                    EVENTS.add("filter-after");
                }
            };

            FilterRegistrationBean<Filter> registration =
                    new FilterRegistrationBean<>(filter);
            registration.addUrlPatterns("/flow");
            return registration;
        }

        @Bean
        FlowController flowController() {
            return new FlowController();
        }
    }

    @RestController
    static class FlowController {

        @GetMapping("/flow")
        String flow() {
            EVENTS.add("controller");
            return "ok";
        }
    }
}
