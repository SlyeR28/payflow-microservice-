package com.payflow.apigateway.config;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
/*
Programmatic route definition
Route map external url patterns to Eureka-resolved services..
 */

@Configuration
public class GateWayRouteConfig {

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute(){
        return GatewayRouterFunctions.route("auth-service")
                .route(
                        request -> request.path().startsWith("/api/v1/auth/") ||
                                   request.path().startsWith("/api/v1/user-profile"),
                        HandlerFunctions.http()
                ).
                filter(lb("auth-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> merchantServiceRoute(){
        return GatewayRouterFunctions.route("merchant-service")
                .route(
                        request -> request.path().startsWith("/api/v1/merchants") ||
                                request.path().startsWith("/api/v1/admin/merchants"),
                        HandlerFunctions.http()
                )
                .filter(lb("merchant-service"))
                .build();
    }

}
