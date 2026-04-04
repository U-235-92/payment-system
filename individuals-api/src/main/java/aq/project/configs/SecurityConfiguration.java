package aq.project.configs;

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
    public SecurityWebFilterChain commonSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    @Profile("dev")
    public SecurityWebFilterChain devSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/gateway/api/user/**", "/dev/**"))
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }

    @Bean
    @Order(3)
    public SecurityWebFilterChain gatewayUserRestControllerSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/gateway/api/user/**"))
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.POST, "/gateway/api/user/create-user").permitAll()
                        .pathMatchers(HttpMethod.POST, "/gateway/api/user/login-user").permitAll()
                        .pathMatchers(HttpMethod.PATCH, "/gateway/api/user/update-user").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/gateway/api/user/delete-user-by-keycloak-id/*").authenticated()
                        .pathMatchers(HttpMethod.GET, "/gateway/api/user/get-user-info").authenticated()
                        .pathMatchers(HttpMethod.POST, "/gateway/api/user/refresh-token").authenticated())
                .build();
    }

    @Bean
    @Order(4)
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
