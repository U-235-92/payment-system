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

import static aq.project.controller.PaymentRestControllerApi.PATH_GET_PAYMENT_METHODS;
import static aq.project.controller.PersonRestControllerApi.*;
import static aq.project.controller.RateRestControllerApi.*;
import static aq.project.controller.TokenRestControllerApi.PATH_REFRESH_TOKEN;
import static aq.project.controller.TransactionRestControllerApi.*;
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
    @Profile({ "prod", "test" })
    public SecurityWebFilterChain gatewayUserRestControllerSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/v1/**"))
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.POST, PATH_CREATE_PERSON).permitAll()
                        .pathMatchers(HttpMethod.POST, PATH_LOGIN_PERSON).permitAll()
                        .pathMatchers(HttpMethod.PATCH, PATH_UPDATE_PERSON).authenticated()
                        .pathMatchers(HttpMethod.DELETE, PATH_DELETE_PERSON.replace("{personKeycloakId}", "*")).authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/person/get-person-info").authenticated()

                        .pathMatchers(HttpMethod.POST, PATH_REFRESH_TOKEN).authenticated()

                        .pathMatchers(HttpMethod.GET, PATH_GET_TRANSACTIONS).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_TRANSACTION_STATUS.replace("{id}", "*")).authenticated()
                        .pathMatchers(HttpMethod.POST, PATH_CONFIRM_TRANSACTION.replace("{type}", "*")).authenticated()
                        .pathMatchers(HttpMethod.POST, PATH_INIT_TRANSACTION.replace("{type}", "*")).authenticated()

                        .pathMatchers(HttpMethod.POST, PATH_CREATE_WALLET).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_WALLET_INFO.replace("{walletId}", "*")).authenticated()

                        .pathMatchers(HttpMethod.GET, PATH_GET_RATE).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_CURRENCIES).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_CURRENCY_INFO.replace("{code}", "*")).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_RATE_PROVIDERS).authenticated()
                        .pathMatchers(HttpMethod.GET, PATH_GET_RATE_PROVIDER_INFO.replace("{code}", "*")).authenticated()

                        .pathMatchers(HttpMethod.GET, PATH_GET_PAYMENT_METHODS.replace("{currencyCode}/{countryCode}", "*/*")).authenticated())
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