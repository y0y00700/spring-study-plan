package webboundary;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = HttpRequestResponseBoundaryTest.TestApplication.class
)
class HttpRequestResponseBoundaryTest {

    @LocalServerPort
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void samePathWithDifferentMethodsAndBodies() throws Exception {
        URI uri = URI.create("http://localhost:" + port + "/boundary");

        HttpResponse<String> getResponse = client.send(
                HttpRequest.newBuilder(uri)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> postHelloResponse = client.send(
                HttpRequest.newBuilder(uri)
                        .header("Content-Type", "text/plain; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString("hello"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> postWorldResponse = client.send(
                HttpRequest.newBuilder(uri)
                        .header("Content-Type", "text/plain; charset=UTF-8")
                        .POST(HttpRequest.BodyPublishers.ofString("world"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        HttpResponse<String> putResponse = client.send(
                HttpRequest.newBuilder(uri)
                        .header("Content-Type", "text/plain; charset=UTF-8")
                        .PUT(HttpRequest.BodyPublishers.ofString("hello"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(getResponse.statusCode()).isEqualTo(200);
        assertThat(getResponse.body()).isEqualTo("GET:no-body");

        assertThat(postHelloResponse.statusCode()).isEqualTo(200);
        assertThat(postHelloResponse.body()).isEqualTo("POST:hello");

        assertThat(postWorldResponse.statusCode()).isEqualTo(200);
        assertThat(postWorldResponse.body()).isEqualTo("POST:world");

        assertThat(putResponse.statusCode()).isEqualTo(405);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(BoundaryController.class)
    static class TestApplication {
    }

    @RestController
    static class BoundaryController {

        @GetMapping(
                value = "/boundary",
                produces = MediaType.TEXT_PLAIN_VALUE
        )
        String getBoundary() {
            return "GET:no-body";
        }

        @PostMapping(
                value = "/boundary",
                consumes = MediaType.TEXT_PLAIN_VALUE,
                produces = MediaType.TEXT_PLAIN_VALUE
        )
        String postBoundary(@RequestBody String body) {
            return "POST:" + body;
        }
    }
}