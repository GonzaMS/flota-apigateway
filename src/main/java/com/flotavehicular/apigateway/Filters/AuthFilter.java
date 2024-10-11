package com.flotavehicular.apigateway.Filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class AuthFilter implements GatewayFilter {

    private final WebClient webClient;
    private final RouteValidator routeValidator;

    public AuthFilter(ReactorLoadBalancerExchangeFilterFunction lbFunction, RouteValidator routeValidator) {
        this.webClient = WebClient.builder()
                .filter(lbFunction)
                .build();
        this.routeValidator = routeValidator;
    }

    @Value("${auth.jwt.validate.url}")
    private String AUTH_VALIDATE_URL;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (routeValidator.isSecured.test(exchange.getRequest())) {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Authorization header is missing");
                return this.onError(exchange, "No authorization header");
            }

            final var tokenHeader = exchange
                    .getRequest()
                    .getHeaders()
                    .get(HttpHeaders.AUTHORIZATION)
                    .get(0);

            final var chunks = tokenHeader.split(" ");
            if (chunks.length != 2 || !chunks[0].equals("Bearer")) {
                log.warn("Invalid token format: {}", tokenHeader);
                return this.onError(exchange, "Invalid token");
            }

            final var token = chunks[1];
            log.info("Extracted token: {}", token);

            return this.webClient
                    .post()
                    .uri(AUTH_VALIDATE_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .flatMap(response -> {
                        log.info("Token validation response: {}", response);
                        if (response.contains("true")) {
                            return chain.filter(exchange);
                        } else {
                            log.warn("Invalid token: {}", token);
                            return this.onError(exchange, "Invalid token");
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Error during token validation: {}", e.getMessage());
                        return this.onError(exchange, "Error during token validation");
                    });
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        final var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        log.error("Authorization error: {}", err);

        final var buffer = response.bufferFactory().wrap(err.getBytes());

        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        return response.writeWith(Mono.just(buffer));
    }
}
