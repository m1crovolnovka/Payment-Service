package org.example.paymentservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.paymentservice.exception.ExternalServiceException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "random-service-client",
        url = "https://www.random.org"
)
public interface RandomServiceClient {

    String CB_NAME = "randomServiceCB";

    @GetMapping("/integers/")
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "getRandomNumberFallback")
    String getRandomNumber(
            @RequestParam("num") int num,
            @RequestParam("min") int min,
            @RequestParam("max") int max,
            @RequestParam("col") int col,
            @RequestParam("base") int base,
            @RequestParam("format") String format,
            @RequestParam("rnd") String rnd
    );

    default String getRandomNumberFallback(int num, int min, int max, int col, int base, String format, String rnd, Throwable ex) {
        throw new ExternalServiceException("Random.org API is currently unavailable. Payment flow interrupted.");
    }
}