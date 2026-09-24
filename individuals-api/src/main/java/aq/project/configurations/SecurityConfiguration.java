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

import static aq.project.controller.DepositTransactionRestControllerApi.PATH_CREATE_DEPOSIT_TRANSACTION;
import static aq.project.controller.DepositTransactionRestControllerApi.PATH_GET_DEPOSIT_TRANSACTION_STATUS;
import static aq.project.controller.RateRestControllerApi.*;
import static aq.project.controller.TokenRestControllerApi.PATH_REFRESH_TOKEN;
import static aq.project.controller.TransferTransactionRestControllerApi.PATH_CREATE_TRANSFER_TRANSACTION;
import static aq.project.controller.TransferTransactionRestControllerApi.PATH_GET_TRANSFER_TRANSACTION_STATUS;
import static aq.project.controller.UserRestControllerApi.*;
import static aq.project.controller.WalletRestControllerApi.PATH_CREATE_WALLET;
import static aq.project.controller.WalletRestControllerApi.PATH_GET_WALLET_INFO;
import static aq.project.controller.WithdrawTransactionRestControllerApi.PATH_CREATE_WITHDRAW_TRANSACTION;
import static aq.project.controller.WithdrawTransactionRestControllerApi.PATH_GET_WITHDRAW_TRANSACTION_STATUS;

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
    @Profile({ "prod", "test" })
    public SecurityWebFilterChain gatewayUserRestControllerSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/v1/**"))
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.POST, PATH_CREATE_USER).permitAll()
                        .pathMatchers(HttpMethod.POST, PATH_LOGIN_USER).permitAll()
                        .pathMatchers(HttpMethod.PATCH, PATH_UPDATE_USER).authenticated()
                        .pathMatchers(HttpMethod.DELETE, PATH_DELETE_USER_BY_KEYCLOAK_ID.replace("{keycloakId}", "*")).authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/user/get-user-info").authenticated()

                        .pathMatchers(HttpMethod.POST, PATH_REFRESH_TOKEN).authenticated()

                        .pathMatchers(HttpMethod.POST, PATH_CREATE_DEPOSIT_TRANSACTION).authenticated()
                        .pathMatchers(HttpMethod.POST, PATH_CREATE_TRANSFER_TRANSACTION).authenticated()
                        .pathMatchers(HttpMethod.POST, PATH_CREATE_WITHDRAW_TRANSACTION).authenticated()

                        .pathMatchers(HttpMethod.GET, PATH_GET_DEPOSIT_TRANSACTION_STATUS.replace("{transactionId}", "*")).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_TRANSFER_TRANSACTION_STATUS.replace("{transactionId}", "*")).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_WITHDRAW_TRANSACTION_STATUS.replace("{transactionId}", "*")).authenticated()

                        .pathMatchers(HttpMethod.POST, PATH_CREATE_WALLET).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_WALLET_INFO.replace("{walletId}", "*")).authenticated()

                        .pathMatchers(HttpMethod.GET, PATH_GET_RATE).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_CURRENCIES).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_CURRENCY_INFO.replace("{code}", "*")).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_RATE_PROVIDERS).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_RATE_PROVIDER_INFO.replace("{code}", "*")).authenticated())
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
