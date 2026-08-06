package mvc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = ValidationExceptionAdviceTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ValidationExceptionAdviceTest {

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
    void typeMismatchIsHandledBeforeController() throws Exception {
        HttpResponse<String> response = post(
                "/mvc-04/orders/abc",
                """
                {"productCode":"BOOK","quantity":1}
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
        assertTrue(response.body().contains(
                "\"code\":\"TYPE_MISMATCH\""
        ));
    }

    @Test
    void validationFailureIsHandledBeforeController() throws Exception {
        HttpResponse<String> response = post(
                "/mvc-04/orders/10",
                """
                {"productCode":"","quantity":0}
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
        assertTrue(response.body().contains(
                "\"code\":\"VALIDATION_FAILED\""
        ));
    }

    @Test
    void businessExceptionIsHandledAfterControllerEntry() throws Exception {
        HttpResponse<String> response = post(
                "/mvc-04/orders/10",
                """
                {"productCode":"SOLD_OUT","quantity":1}
                """
        );

        assertEquals(
                409,
                response.statusCode()
        );
        assertEquals(
                1,
                controller.invocationCount()
        );
        assertTrue(response.body().contains(
                "\"code\":\"SOLD_OUT\""
        ));
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
    @Import({
            OrderController.class,
            GlobalExceptionHandler.class
    })
    static class TestApplication {
    }

    @RestController
    static class OrderController {

        private final AtomicInteger invocationCount = new AtomicInteger();

        @PostMapping("/mvc-04/orders/{id}")
        OrderResponse create(
                @PathVariable("id") long id,
                @Valid @RequestBody CreateOrderRequest request
        ) {
            invocationCount.incrementAndGet();

            if ("SOLD_OUT".equals(request.productCode())) {
                throw new SoldOutException();
            }

            return new OrderResponse(id);
        }

        int invocationCount() {
            return invocationCount.get();
        }

        void reset() {
            invocationCount.set(0);
        }
    }

    @RestControllerAdvice
    static class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        ResponseEntity<ApiError> handleTypeMismatch(
                MethodArgumentTypeMismatchException exception
        ) {
            return ResponseEntity
                    .status(400)
                    .body(new ApiError(
                            "TYPE_MISMATCH"
                    ));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        ResponseEntity<ApiError> handleValidation(
                MethodArgumentNotValidException exception
        ) {
            return ResponseEntity
                    .status(400)
                    .body(new ApiError(
                            "VALIDATION_FAILED"
                    ));
        }

        @ExceptionHandler(SoldOutException.class)
        ResponseEntity<ApiError> handleSoldOut(
                SoldOutException exception
        ) {
            return ResponseEntity
                    .status(409)
                    .body(new ApiError(
                            "SOLD_OUT"
                    ));
        }
    }

    record CreateOrderRequest(
            @NotBlank String productCode,
            @Min(1) int quantity
    ) {
    }

    record OrderResponse(long id) {
    }

    record ApiError(String code) {
    }

    static class SoldOutException extends RuntimeException {
    }
}