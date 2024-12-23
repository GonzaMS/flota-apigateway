package com.flotavehicular.apigateway.Config;

import com.flotavehicular.apigateway.Filters.AuthFilter;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@AllArgsConstructor
public class GatewayConfig {

    private final AuthFilter authFilter;

    @Bean
    @Profile("eureka-off")
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(route -> route
                        .path("/api/v1/cars/**")
                        .uri("http://localhost:8080")
                )
                .route(route -> route
                        .path("/api/v1/auth/**")
                        .uri("http://localhost:8081")
                )
                .build();
    }

    @Bean
    @Profile("eureka-on")
    public RouteLocator routeLocatorEurekaOn(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(route -> route
                        .path("/api/v1/cars/**")
                        .uri("lb://car-microservice")
                )
                .route(route -> route
                        .path("/api/v1/auth/**")
                        .uri("lb://security-microservice")
                )
                .build();
    }

    @Bean
    @Profile("security")
    public RouteLocator routeLocatorSecurity(RouteLocatorBuilder builder) {
        return builder
                .routes()

//                Car microservices
                .route(route -> route
                        .path("/api/v1/cars/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/cars")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://car-microservice"))

                .route(route -> route
                        .path("/api/v1/kilometers/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/kilometers")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://car-microservice"))

                .route(route -> route
                        .path("/api/v1/incidents/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/incidents")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://car-microservice"))

                .route(route -> route
                        .path("/api/v1/car-reports/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/car-reports")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://car-microservice"))


                .route(route -> route
                        .path("/api/v1/maintenances/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/maintenances")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://car-microservice"))

                .route(route -> route
                        .path("/api/v1/roles/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/roles")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://security-microservice"))


//                Driver microservice
                .route(route -> route
                        .path("/api/v1/drivers/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/drivers")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://driver-microservice"))

                .route(route -> route
                        .path("/api/v1/driving_incidents/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/incidents")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://driver-microservice"))

                .route(route -> route
                        .path("/api/v1/driving-history/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/driving-history")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://driver-microservice"))

                .route(route -> route
                        .path("/api/v1/performance-evaluations/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/performance-evaluations")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://driver-microservice"))

                .route(route -> route
                        .path("/api/v1/assigned-orders/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/assigned-orders")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://driver-microservice"))



//               Security microservice
                .route(route -> route
                        .path("/api/v1/users/**")
                        .filters(filter -> {
                                    filter.circuitBreaker(config -> config
                                            .setName("gateway-cb")
                                            .setFallbackUri("forward:/api/v1/fallback/users")
                                    );
                                    filter.filter(this.authFilter);
                                    return filter;
                                }
                        )
                        .uri("lb://security-microservice"))


                .route(route -> route
                        .path("/api/v1/auth/**")
                        .uri("lb://security-microservice"))
                .build();
    }

}
