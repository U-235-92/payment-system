package aq.project.configurations;

import jakarta.ws.rs.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    @Order(1)
    @Profile("dev")
    public SecurityWebFilterChain devSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/api/user/**",
                        "/api/token/**",
                        "/api/transaction/**",
                        "/api/wallet/**",
                        "/api/currency-rates/**"))
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    @Profile("prod")
    public SecurityWebFilterChain gatewayUserRestControllerSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/api/user/**",
                        "/api/token/**",
                        "/api/transaction/**",
                        "/api/wallet/**",
                        "/api/currency-rates/**"))
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.POST, "/api/user/create-user").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/user/login-user").permitAll()
                        .pathMatchers(HttpMethod.PATCH, "/api/user/update-user").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/user/delete-user-by-keycloak-id/*").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/user/get-user-info").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/token/refresh-token").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/transaction/status/*").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/transaction/do-transaction").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/wallet/create").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/wallet/info/*").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/currency-rates/rate").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/currency-rates/currencies").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/currency-rates/rate-providers").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/currency-rates/currency/*").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/currency-rates/rate-provider/*").permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(3)
    public SecurityWebFilterChain actuatorSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/actuator/**"))
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
