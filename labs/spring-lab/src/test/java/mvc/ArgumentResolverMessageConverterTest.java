package mvc;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = ArgumentResolverMessageConverterTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ArgumentResolverMessageConverterTest {

    @LocalServerPort
    int port;

    @Autowired
    OrderController controller;

    HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void resetController() {
        controller.reset();
    }

    @Test
    void resolvesThreeArgumentsAndWritesJsonResponse() throws Exception {
        HttpResponse<String> response = post(
                "/mvc-03/orders/10?urgent=true",
                """
                {"name":"book"}
                """
        );

        assertEquals(
                200,
                response.statusCode()
        );
        assertEquals(
                1,
                controller.invocationCount()
        );

        assertTrue(response.body().contains(
                "\"id\":10"
        ));
        assertTrue(response.body().contains(
                "\"urgent\":true"
        ));
        assertTrue(response.body().contains(
                "\"name\":\"book\""
        ));
    }

    @Test
    void invalidPathVariableStopsBeforeController() throws Exception {
        HttpResponse<String> response = post(
                "/mvc-03/orders/abc?urgent=true",
                """
                {"name":"book"}
                """
        );

        assertEquals(
                400,
                response.statusCode()
        );
        assertEquals(
                0,
                controller.invocationCount()
        );
    }

    @Test
    void missingRequestParameterStopsBeforeController() throws Exception {
        HttpResponse<String> response = post(
                "/mvc-03/orders/10",
                """
                {"name":"book"}
                """
        );

        assertEquals(
                400,
                response.statusCode()
        );
        assertEquals(
                0,
                controller.invocationCount()
        );
    }

    @Test
    void malformedJsonStopsBeforeController() throws Exception {
        HttpResponse<String> response = post(
                "/mvc-03/orders/10?urgent=true",
                """
                {"name":
                """
        );

        assertEquals(
                400,
                response.statusCode()
        );
        assertEquals(
                0,
                controller.invocationCount()
        );
    }

    private HttpResponse<String> post(String path, String body)
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(OrderController.class)
    static class TestApplication {
    }

    @RestController
    static class OrderController {

        private final AtomicInteger invocationCount = new AtomicInteger();

        @PostMapping("/mvc-03/orders/{id}")
        OrderResponse create(
                @PathVariable("id") long id,
                @RequestParam("urgent") boolean urgent,
                @RequestBody CreateOrderRequest request
        ) {
            invocationCount.incrementAndGet();
            return new OrderResponse(id, urgent, request.name());
        }

        int invocationCount() {
            return invocationCount.get();
        }

        void reset() {
            invocationCount.set(0);
        }
    }

    record CreateOrderRequest(String name) {
    }

    record OrderResponse(long id, boolean urgent, String name) {
    }
}