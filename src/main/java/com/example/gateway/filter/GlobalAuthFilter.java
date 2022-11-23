package com.example.gateway.filter;

import com.example.gateway.validation.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GlobalAuthFilter {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;


    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("token").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("token");
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (this.isAuthMissing(request)) {
                return jwtUtil.onError(exchange, "token header is missing in request", HttpStatus.UNAUTHORIZED);
            }
            final String token = this.getAuthHeader(request);
            if (jwtUtil.isInvalid(token)) {
                return jwtUtil.onError(exchange, "token header is invalid", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        }));
    }
}
