package aq.project.configs;

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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Bean
    @Order(1)
    @Profile("dev")
    public SecurityFilterChain h2SecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/h2-console/**", "/dev/**")
                .authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
                .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .cors(CorsConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain commonSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) {
        return http
                .securityMatcher("/v1/person/**")
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers(HttpMethod.POST, "/create").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/undo-create/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/delete/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/update/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/get/*").authenticated())
                .build();

    }

    @Bean
    @Order(4)
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
