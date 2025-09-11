package co.com.crediya.api.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
    @GetMapping("/test/headers")
    public Mono<String> testHeaders() {
        return Mono.just("test");
    }
}
