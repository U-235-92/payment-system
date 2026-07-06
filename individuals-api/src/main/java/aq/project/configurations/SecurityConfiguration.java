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

import static aq.project.controller.RateRestControllerApi.*;
import static aq.project.controller.TokenRestControllerApi.PATH_REFRESH_TOKEN;
import static aq.project.controller.TransactionRestControllerApi.PATH_DO_TRANSACTION;
import static aq.project.controller.TransactionRestControllerApi.PATH_GET_TRANSACTION_STATUS;
import static aq.project.controller.UserRestControllerApi.*;
import static aq.project.controller.WalletRestControllerApi.PATH_CREATE_WALLET;
import static aq.project.controller.WalletRestControllerApi.PATH_GET_WALLET_INFO;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    @Order(1)
    @Profile("dev")
    public SecurityWebFilterChain devSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/v1/**"))
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
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/v1/**"))
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.POST, "/api/v1" + PATH_CREATE_USER).permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1" + PATH_LOGIN_USER).permitAll()
                        .pathMatchers(HttpMethod.PATCH, "/api/v1" + PATH_UPDATE_USER).authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/v1" + PATH_DELETE_USER_BY_KEYCLOAK_ID.replace("{keycloakId}", "*")).authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/user/get-user-info").authenticated()

                        .pathMatchers(HttpMethod.POST, "/api/v1" + PATH_REFRESH_TOKEN).authenticated()

                        .pathMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_TRANSACTION_STATUS.replace("{transactionId}", "*")).authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1" + PATH_DO_TRANSACTION).authenticated()

                        .pathMatchers(HttpMethod.POST, "/api/v1" + PATH_CREATE_WALLET).authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_WALLET_INFO.replace("{walletId}", "*")).authenticated()

                        .pathMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_RATE).permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_CURRENCIES).permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_CURRENCY_INFO.replace("{code}", "*")).permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_RATE_PROVIDERS).permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_RATE_PROVIDER_INFO.replace("{code}", "*")).permitAll())
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
