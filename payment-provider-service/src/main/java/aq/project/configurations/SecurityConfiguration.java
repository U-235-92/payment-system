package aq.project.configurations;

import aq.project.dto.UserRole;
import aq.project.repositories.MerchantRepository;
import aq.project.utils.security.AuthenticationExceptionEntryPoint;
import aq.project.utils.security.AuthenticationHeaderFilter;
import aq.project.utils.security.MerchantDetailsService;
import aq.project.utils.security.MerchantProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static aq.project.controller.AdminRestControllerApi.PATH_REGISTER_MERCHANT;
import static aq.project.controller.TransactionRestControllerApi.*;
import static aq.project.controller.WebhookRestControllerApi.PATH_UPDATE_TRANSACTION_STATUS;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityConfiguration(HttpSecurity http) {
        return http
            .securityMatcher("/api/v1/**")
            .cors(CorsConfigurer::disable)
            .csrf(CsrfConfigurer::disable)
            .httpBasic(c -> c.authenticationEntryPoint(new AuthenticationExceptionEntryPoint(handlerExceptionResolver)))
            .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .authorizeHttpRequests(customizer -> customizer.anyRequest().permitAll())
            .build();
    }

    @Bean
    @Profile({ "prod", "test" })
    public SecurityFilterChain prodSecurityConfiguration(HttpSecurity http) {
        return http
            .securityMatcher("/api/v1/**")
            .cors(CorsConfigurer::disable)
            .csrf(CsrfConfigurer::disable)
            .httpBasic(c -> c.authenticationEntryPoint(new AuthenticationExceptionEntryPoint(handlerExceptionResolver)))
            .headers(customizer -> customizer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .authorizeHttpRequests(customizer -> customizer
                    .requestMatchers(HttpMethod.POST, PATH_CREATE_TRANSACTION).hasRole(UserRole.USER.getValue())
                    .requestMatchers(HttpMethod.GET, PATH_GET_TRANSACTION_INFO.replace("{id}", "*")).hasRole(UserRole.USER.getValue())
                    .requestMatchers(HttpMethod.GET, PATH_GET_TRANSACTION_LIST).hasRole(UserRole.USER.getValue())
                    .requestMatchers(HttpMethod.POST, PATH_UPDATE_TRANSACTION_STATUS).hasRole(UserRole.USER.getValue())
                    .requestMatchers(HttpMethod.POST, PATH_CANCEL_TRANSACTION.replace("{id}", "*")).hasRole(UserRole.USER.getValue())
                    .requestMatchers(HttpMethod.POST, PATH_REGISTER_MERCHANT).hasRole(UserRole.ADMIN.getValue())
                    .anyRequest().authenticated()
            )
            .addFilterBefore(new AuthenticationHeaderFilter(), BasicAuthenticationFilter.class)
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
