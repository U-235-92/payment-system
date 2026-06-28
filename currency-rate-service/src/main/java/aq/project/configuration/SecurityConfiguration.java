package aq.project.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static aq.project.controller.RatesRestControllerApi.*;

@Configuration
public class SecurityConfiguration {

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityConfiguration(HttpSecurity http) {
        return http
            .securityMatcher("/h2-console/**", "/api/v1/**")
            .cors(CorsConfigurer::disable)
            .csrf(CsrfConfigurer::disable)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
            .build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityConfiguration(HttpSecurity http) {
        return http
                .securityMatcher("/api/v1/**")
                .cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_CURRENCIES).authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_RATE).authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1" + PATH_GET_RATE_PROVIDERS).authenticated())
                .build();
    }

    @Bean
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
