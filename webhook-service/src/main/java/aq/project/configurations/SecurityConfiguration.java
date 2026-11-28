package aq.project.configurations;

import aq.project.security.AccessTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;

import static aq.project.controller.WebhookRestControllerApi.PATH_CONFIRM_OPERATION_WEBHOOK;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Value("${app.security.access-token-key}")
    private String serviceAccessTokenKey;

    @Value("${app.security.access-token-hash-alg}")
    private String serviceAccessTokenHashAlg;

    @Bean
    @Order(1)
    @Profile(value = { "dev" })
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/h2-console/**", "/api/v1/**")
                .authorizeHttpRequests(customizer -> customizer
                        .anyRequest().permitAll())
                .addFilterBefore(new AccessTokenFilter(serviceAccessTokenKey, serviceAccessTokenHashAlg), AuthenticationFilter.class)
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
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.POST, PATH_CONFIRM_OPERATION_WEBHOOK).permitAll())
                .addFilterBefore(new AccessTokenFilter(serviceAccessTokenKey, serviceAccessTokenHashAlg), AuthenticationFilter.class)
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