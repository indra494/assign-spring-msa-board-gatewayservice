package com.indra.gatewayservice.filter;

import com.indra.gatewayservice.domain.MemberRedis;
import com.indra.gatewayservice.repository.MemberRedisRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Optional;


@Component
@Slf4j
public class AuthorizationRefreshHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationRefreshHeaderFilter.Config> {
    private final Environment env;
    private final MemberRedisRepository memberRedisRepository;

    public static class Config {
    }

    public AuthorizationRefreshHeaderFilter(Environment env, MemberRedisRepository memberRedisRepository) {
        super(Config.class);
        this.env = env;
        this.memberRedisRepository = memberRedisRepository;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (((exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer", "");

            String refreshScretKey = env.getProperty("jwt.refresh-token.secret");
            if(isJwtValid(jwt, refreshScretKey) > 0) {
                return onError(exchange, "JWT token is not valid.", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        }));

    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        log.error(err);
        return response.setComplete();
    }

    private int isJwtValid(String jwt, String secretKey) {
        int returnValue = 0;
        String subject = null;

        try {
            subject = getJwtSubject(jwt, secretKey);
        } catch(ExpiredJwtException je) {
            returnValue = 2;
        } catch(Exception ex) {
            returnValue = 1;
        }

        if(subject == null || subject.isEmpty()) {
            returnValue = 1;
        }

        return returnValue;
    }

    private Long getJwtId(String jwt, String secretKey) {
        Long id = Long.parseLong(Jwts.parser().setSigningKey(secretKey)
                    .parseClaimsJws(jwt).getBody().get("id").toString());
        return id;
    }

    private String getJwtSubject(String jwt, String secretKey) {
        String subject = Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(jwt).getBody()
                .getSubject();
        return subject;
    }

}
