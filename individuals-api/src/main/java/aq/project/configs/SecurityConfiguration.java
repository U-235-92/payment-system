package aq.project.configs;

import jakarta.ws.rs.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(customizer -> customizer
                        .pathMatchers(HttpMethod.POST, "/v1/auth/registration").permitAll()
                        .pathMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/v1/auth/context").permitAll()
                        .pathMatchers(HttpMethod.POST, "/v1/auth/refresh-token").permitAll()
                        .pathMatchers(HttpMethod.GET, "/v1/auth/me").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }
}
