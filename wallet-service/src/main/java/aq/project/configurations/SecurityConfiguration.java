package aq.project.configurations;

import aq.project.utils.security.HeaderValidatorHttpFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static aq.project.controller.DepositTransactionRestControllerApi.PATH_GET_DEPOSIT_TRANSACTION_STATUS;
import static aq.project.controller.TransferTransactionRestControllerApi.PATH_GET_TRANSFER_TRANSACTION_STATUS;
import static aq.project.controller.WalletRestControllerApi.*;
import static aq.project.controller.WithdrawTransactionRestControllerApi.PATH_GET_WITHDRAW_TRANSACTION_STATUS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    @Order(1)
    @Profile(value = { "dev", "dev-shard" })
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/h2-console/**", "/api/v1/**")
                .authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .addFilterBefore(new HeaderValidatorHttpFilter(), BasicAuthenticationFilter.class)
                .cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    @Profile(value = { "prod", "test" })
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.POST, PATH_CREATE_WALLET).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_WALLET_INFO.replace("{id}", "*")).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_WALLET_CURRENCY.replace("{id}", "*")).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_DEPOSIT_TRANSACTION_STATUS.replace("{id}", "*")).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_WITHDRAW_TRANSACTION_STATUS.replace("{id}", "*")).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_TRANSFER_TRANSACTION_STATUS.replace("{id}", "*")).authenticated())
                .addFilterBefore(new HeaderValidatorHttpFilter(), BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll())
                .build();
    }
}
