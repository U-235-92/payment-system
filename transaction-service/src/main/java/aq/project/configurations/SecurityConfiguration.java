package aq.project.configurations;

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
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    @Order(1)
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/api/transactions/**")
                .authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(CsrfConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    @Profile(value = { "prod-local", "prod-container" })
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/api/transactions/**")
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.POST, "/api/transactions/send").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/transactions/status/*").authenticated())
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
