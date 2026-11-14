package aq.project.configurations;

import aq.project.repositories.MerchantRepository;
import aq.project.security.AuthenticationExceptionEntryPoint;
import aq.project.security.AuthenticationHeaderFilter;
import aq.project.security.MerchantDetailsService;
import aq.project.security.MerchantProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static aq.project.controller.PayoutRestControllerApi.*;
import static aq.project.controller.TransactionRestControllerApi.*;
import static aq.project.controller.WebhookRestControllerApi.PATH_CONFIRM_PAYOUT_WEBHOOK;
import static aq.project.controller.WebhookRestControllerApi.PATH_CONFIRM_TRANSACTION_WEBHOOK;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    @Order(1)
    @Profile(value = { "dev" })
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/h2-console/**", "/api/v1/**")
                .httpBasic(c -> c.authenticationEntryPoint(new AuthenticationExceptionEntryPoint(handlerExceptionResolver)))
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.POST, PATH_CONFIRM_TRANSACTION_WEBHOOK).denyAll()
                        .requestMatchers(HttpMethod.POST, PATH_CONFIRM_PAYOUT_WEBHOOK).denyAll()
                        .anyRequest().permitAll())
                .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
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
                .httpBasic(c -> c.authenticationEntryPoint(new AuthenticationExceptionEntryPoint(handlerExceptionResolver)))
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.POST, PATH_CREATE_TRANSACTION).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_TRANSACTION_LIST).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_TRANSACTION_INFO.replace("{id}", "*")).authenticated()
                        .requestMatchers(HttpMethod.POST, PATH_CREATE_PAYOUT).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_PAYOUT_LIST).authenticated()
                        .requestMatchers(HttpMethod.GET, PATH_GET_PAYOUT_INFO.replace("{id}", "*")).authenticated()
                        .requestMatchers(HttpMethod.POST, PATH_CONFIRM_TRANSACTION_WEBHOOK).denyAll()
                        .requestMatchers(HttpMethod.POST, PATH_CONFIRM_PAYOUT_WEBHOOK).denyAll())
                .addFilterBefore(new AuthenticationHeaderFilter(), BasicAuthenticationFilter.class)
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            MerchantRepository merchantRepository
    ) {
        return new MerchantDetailsService(merchantRepository);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService
    ) {
        return new MerchantProvider(userDetailsService, passwordEncoder);
    }
}