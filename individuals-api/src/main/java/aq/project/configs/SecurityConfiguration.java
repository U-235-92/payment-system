package aq.project.configs;

import jakarta.ws.rs.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityWebFilterChain commonSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain authControllerSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/v1/auth/**"))
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.POST, "/v1/auth/registration").permitAll()
                        .pathMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/v1/auth/context").permitAll()
                        .pathMatchers(HttpMethod.POST, "/v1/auth/refresh-token").permitAll()
                        .pathMatchers(HttpMethod.GET, "/v1/auth/me").permitAll())
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
                .build();
    }
}
