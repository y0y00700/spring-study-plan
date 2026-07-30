package webboundary;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class ServletLifecycleContainerTest {

    private static final List<String> EVENTS =
            new CopyOnWriteArrayList<>();

    @Test
    void containerCreatesAndCallsServletLifecycle() throws Exception {
        EVENTS.clear();

        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(TestApplication.class)
                        .web(WebApplicationType.SERVLET)
                        .properties(
                                "server.port=0",
                                "spring.main.banner-mode=off"
                        )
                        .registerShutdownHook(false)
                        .run();

        try {
            WebServerApplicationContext webContext =
                    (WebServerApplicationContext) context;

            int port = webContext.getWebServer().getPort();
            URI uri = URI.create(
                    "http://localhost:" + port + "/lifecycle"
            );

            // 서버 시작 시점의 이벤트를 예측해 작성
            assertThat(EVENTS).containsExactly(
                    "constructor","init"
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .GET()
                    .build();

            HttpResponse<String> firstResponse = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            HttpResponse<String> secondResponse = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            assertThat(firstResponse.statusCode()).isEqualTo(200);
            assertThat(secondResponse.statusCode()).isEqualTo(200);

            // 두 요청을 같은 Servlet 인스턴스가 처리했는지 검증
            assertThat(secondResponse.body()).isEqualTo(
                    firstResponse.body()
            );

            // 두 요청 후 전체 이벤트 순서를 예측해 작성
            assertThat(EVENTS).containsExactly(
                    "constructor","init","service","service"
            );
        }
        finally {
            context.close();
        }

        // 서버 종료 후 전체 이벤트 순서를 예측해 작성
        assertThat(EVENTS).containsExactly(
                "constructor","init","service","service","destroy"
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {

        @Bean
        ServletContextInitializer lifecycleServletRegistration() {
            return servletContext -> {
                var registration = servletContext.addServlet(
                        "lifecycleServlet",
                        LifecycleServlet.class
                );
                registration.addMapping("/lifecycle");
                registration.setLoadOnStartup(1);
            };
        }
    }

    public static class LifecycleServlet extends HttpServlet {

        public LifecycleServlet() {
            EVENTS.add("constructor");
        }

        @Override
        public void init(ServletConfig config)
                throws ServletException {
            super.init(config);
            EVENTS.add("init");
        }

        @Override
        protected void service(
                HttpServletRequest request,
                HttpServletResponse response
        ) throws ServletException, IOException {
            EVENTS.add("service");

            response.setContentType("text/plain");
            response.getWriter().write(
                    Integer.toString(System.identityHashCode(this))
            );
        }

        @Override
        public void destroy() {
            EVENTS.add("destroy");
            super.destroy();
        }
    }
}