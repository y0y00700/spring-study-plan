package tomcat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(
        classes = TomcatThreadSharedStateTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class TomcatThreadSharedStateTest {

    @Value("${local.server.port}")
    int port;

    @Test
    void concurrentRequestsOverwriteSingletonField() throws Exception {
        var executor = Executors.newFixedThreadPool(2);

        try {
            var aFuture = executor.submit(() -> sendRequest("A"));
            var bFuture = executor.submit(() -> sendRequest("B"));

            Observation a = aFuture.get(10, TimeUnit.SECONDS);
            Observation b = bFuture.get(10, TimeUnit.SECONDS);

            // 실행 전에 "?"를 예측값으로 교체하세요.
            assertEquals("B", a.storedUser());
            assertEquals("B", b.storedUser());

            // 두 요청이 같은 Singleton Bean을 사용한다.
            assertEquals(a.instanceId(), b.instanceId());

            // 동시에 진행된 요청은 서로 다른 Tomcat 스레드가 처리한다.
            assertNotEquals(a.threadName(), b.threadName());
        } finally {
            executor.shutdownNow();
        }
    }

    private Observation sendRequest(String user) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port
                                + "/shared-state?user=" + user
                ))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        return Observation.parse(response.body());
    }

    record Observation(
            String requestedUser,
            String storedUser,
            String instanceId,
            String threadName
    ) {
        static Observation parse(String body) {
            String[] values = body.split("\\|", -1);
            return new Observation(
                    values[0],
                    values[1],
                    values[2],
                    values[3]
            );
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        SharedStateController sharedStateController() {
            return new SharedStateController();
        }
    }

    @RestController
    static class SharedStateController {

        private final CountDownLatch aStored = new CountDownLatch(1);
        private final CountDownLatch bStored = new CountDownLatch(1);

        private String currentUser;

        @GetMapping("/shared-state")
        String handle(@RequestParam String user) throws InterruptedException {
            String threadName = Thread.currentThread().getName();

            if ("A".equals(user)) {
                currentUser = user;
                aStored.countDown();

                if (!bStored.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("B 요청 대기 시간 초과");
                }
            } else {
                if (!aStored.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("A 요청 대기 시간 초과");
                }

                currentUser = user;
                bStored.countDown();
            }

            return user
                    + "|" + currentUser
                    + "|" + System.identityHashCode(this)
                    + "|" + threadName;
        }
    }
}